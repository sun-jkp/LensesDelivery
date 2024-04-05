package com.transition.lensesdelivery.presentation.delivery_confirm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reeman.ros.controller.RobotActionController
import com.reeman.ros.listen.RosCallBackListener
import com.transition.lensesdelivery.data.local.QueueDatabase
import com.transition.lensesdelivery.data.mapper.toQueue
import com.transition.lensesdelivery.data.mapper.toQueueEntity
import com.transition.lensesdelivery.domain.model.Queue
import com.transition.lensesdelivery.domain.repository.QueueRepository
import com.transition.lensesdelivery.domain.repository.SocketRepository
import com.transition.lensesdelivery.util.Logger
import com.transition.lensesdelivery.util.Resource
import com.transition.lensesdelivery.util.getDateTimeStr
import com.transition.lensesdelivery.util.parseResultStr
import com.transition.lensesdelivery.util.pointIdToPointName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

const val ROS_TAG = "ROS"

@HiltViewModel
class DeliveryViewModel @Inject constructor(
    private val repository: QueueRepository,
    private val socketRepository: SocketRepository,
    private val db: QueueDatabase,
//    val player: Player
) : ViewModel(), RosCallBackListener {

    private val _deliveryState = MutableStateFlow(DeliveryState())
    val deliveryState = _deliveryState.asStateFlow()

    private var navigateJob: Job? = null

    private var controller: RobotActionController? = null

    private val rsId: Int = 1

    private val dao = db.dao

    init {
        resetDBAndNavList()
        listenServer()
        Logger.log("Robot Started")
        Logger.log("State: ${_deliveryState.value}")
        viewModelScope.launch {Logger.log("Local DB: ${dao.searchQueue()}")  }
    }

    private fun resetDBAndNavList() {
        Logger.log("[resetDBAndNavList] Reset Local DB And NavList")
        updateNavList(NavListState(
            listPointName = emptyList(),
            lastIndex = 0
        ))
        viewModelScope.launch {
            async {dao.clearQueue()}
        }
    }

    override fun onCleared() {
        super.onCleared()
        socketDisconnect()
        stopListen()
        navigateJob?.cancel()
    }

    fun pauseBackgroundTask() {
        navigateJob?.cancel()
    }

    fun resumeBackgroundTask() {
        if (navigateJob?.isActive == false) {
            checkQueueInCache()
        }
    }

    fun onEvent(event: QueueEvent) {
        when (event) {
            is QueueEvent.Refresh -> {
                getQueues(fetchFromRemote = true, rsId = rsId)
            }

            is QueueEvent.OnConfirm -> {
                buttonEnable(event.buttonId, false)
                Logger.log("Event Confirm: button ${event.buttonId}")
                navigateController(true)
            }
        }
    }

    fun onRosEvent(event: RosEvent) {
        when (event) {
            is RosEvent.GetHost -> {
                controller?.getHostIp()
                controller?.getHostName()
            }

            is RosEvent.HeartBeats -> {
                heartBeats()
            }

            is RosEvent.GetSpecialArea -> {

                controller?.getSpecialArea()
            }
        }
    }

    //get queue from server and save to local db
    private fun getQueues(
        fetchFromRemote: Boolean = false,
        rsId: Int
    ) {
//        Log.d(TAG, "getQueues")
        Logger.log("[getQueues] Function")
        viewModelScope.launch {
            repository
                .getQueuesFlow(fetchFromRemote, rsId)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            Logger.log("[getQueues] Success: ${result.data}")
                            result.data?.let { listings ->
                                if (listings.isNotEmpty()) {
                                    getQueueToWork()
                                } else {
                                    updateQueueState(null)
                                }
                                _deliveryState.update { it.copy(isConnected = true) }
                            }
                        }

                        is Resource.Error -> {
                            Logger.log("[getQueues] Error:${result.message}")
                            _deliveryState.update { it.copy(isConnected = false) }
                        }

                        is Resource.Loading -> {
                            Logger.log("[getQueues] Loaded")
                        }
                    }
                }
        }
    }

    //select queue from local db to work
    private fun getQueueToWork() {
//        Log.d(TAG, "getQueueToWork")
        Logger.log("[getQueueToWork] Function")
        viewModelScope.launch {
            val queueLocalResult = async{dao.searchQueueOne()}
            val queueE = queueLocalResult.await()
            Logger.log("[getQueueToWork] Find Queue LocalDB: $queueE")
            if ( queueE!= null) {
                Logger.log("[getQueueToWork] Find Queue LocalDB: not null")
                val queue = queueE.toQueue()
                Logger.log("[getQueueToWork] Convert to Queue: $queue")
//                Log.d(ROS_TAG, "$queue")
                updateQueueState(queue)
                resetNavList()
                updateNavigateState(true)
                if (queue.STATUS_ID == 1) {
                    Logger.log("[getQueueToWork] Queue Status equal 1, call navCon(true)")
                    navigateController(true)
                } else {
                    Logger.log("[getQueueToWork] Queue Status not equal 1, call navCon(false)")
                    navigateController(false)
                }
            }else{
                Logger.log("[getQueueToWork] Find Queue LocalD: null")
            }
        }
    }

    //check queue success(status_id=6,7) then sync to server and delete from local db
    private fun checkQueueOnServerAndSync() {
//        Log.d(TAG, "checkQueueOnServerAndSync")
        Logger.log("[checkQueueOnServerAndSync] Function")
        viewModelScope.launch {
            val queueLocalResult = async{dao.searchQueueSuccess()}
            val queuesEntity = queueLocalResult.await()
            Logger.log("[checkQueueOnServerAndSync] Find Success Queue LocalDB: $queuesEntity")
            if (queuesEntity.isEmpty()){
                Logger.log("[checkQueueOnServerAndSync] Not found success queues, return")
                return@launch
            }

            val queues = queuesEntity.map { it.toQueue() }
            Logger.log("[checkQueueOnServerAndSync] Convert to Queue: $queues")
            Logger.log("[checkQueueOnServerAndSync] For Loop Start")
            for (queue in queues) {
                Logger.log("[checkQueueOnServerAndSync] $queue")
                val updateQueueResult = async {
                    repository
                        .updateQueue(queue)
                }

                when (val result = updateQueueResult.await()) {
                    is Resource.Success -> {
                       Logger.log("[checkQueueOnServerAndSync] Update to Server Success and clear queue in local")
                        _deliveryState.update { it.copy(isConnected = true) }
                        dao.clearQueueById(queue.QUEUE_ID)
                    }

                    is Resource.Error -> {
//                        Log.d(TAG, "Update queue to server failed")
                        Logger.log("[checkQueueOnServerAndSync] Update to Server Error: ${result.message}, continue")
                        _deliveryState.update { it.copy(isConnected = false) }
                        continue
                    }
                    else ->{
                        Logger.log("[checkQueueOnServerAndSync] Loading")
                    }
                }
            }
        }
    }

    //check queue in server and local if cancel in server then update in local
    private fun syncQueueById() {
//        Log.d(TAG, "syncQueueById")
        Logger.log("[syncQueueById] Function")
        viewModelScope.launch {
            if (_deliveryState.value.queue != null) {
                val queueByIdResult = async {
                    repository
                        .getQueueById(_deliveryState.value.queue!!.QUEUE_ID)
                }
                when (val result = queueByIdResult.await()) {
                    is Resource.Success -> {
                        result.data?.let {
                            Logger.log("[syncQueueById] Get queue from server success: ${result.data}")
                            if (result.data.STATUS_ID == 8) {
                                val localQueue = _deliveryState.value.queue
                                localQueue?.let {
                                    localQueue.STATUS_ID = result.data.STATUS_ID
                                    Logger.log("[syncQueueById] Queue status in server equal 8 (cancel)")
                                    updateQueueState(localQueue)
                                    Logger.log("[syncQueueById] Update queue in local DB")
                                    dao.updateQueue(localQueue.toQueueEntity())
                                }
                            }
                        }

//                        Log.d(TAG, "Sync queue success")
                        _deliveryState.update { it.copy(isConnected = true) }
                    }

                    is Resource.Error -> {
                        Logger.log("[syncQueueById] Sync failed: ${result.message}")
//                        Log.d(TAG, "Sync queue failed")
                        _deliveryState.update { it.copy(isConnected = false) }
                    }

                    else -> Unit
                }
            }

        }
    }

    //Update queue to server
    private fun updateQueue(queue: Queue) {
        Logger.log("[updateQueue] Function")
        viewModelScope.launch {
            updateQueueState(queue)
            Logger.log("[updateQueue] Update queue in localDB: $queue")
            dao.updateQueue(queue.toQueueEntity())

            val updateQueueResult = async {
                repository
                    .updateQueue(queue)
            }
            when (val result = updateQueueResult.await()) {
                is Resource.Success -> {
                    Logger.log("[updateQueue] Update queue to server success")
                    _deliveryState.update { it.copy(isConnected = true) }
                }

                is Resource.Error -> {
//                    Log.d(TAG, "Update queue to server failed")
                    Logger.log("[updateQueue] Update queue to server error: ${result.message}")
                    _deliveryState.update { it.copy(isConnected = false) }
                }

                else -> Unit
            }
        }
    }


    // loop check queue in local
    fun checkQueueInCache() {
//        Log.d(TAG, "checkQueueInCache")
        Logger.log("[checkQueueInCache] Function")
        navigateJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                onRosEvent(RosEvent.GetSpecialArea)
                sendDataToServer("localQueue: ${dao.searchQueue()}")
                sendDataToServer("RobotState: ${_deliveryState.value}")
                if (_deliveryState.value.isNavigate) {
                    if (_deliveryState.value.queue != null) {
                        Logger.log("[checkQueueInCache] Queue State isn't null, navCon(false)")
                        navigateController(false)
                    } else {
                        Logger.log("[checkQueueInCache] Queue State is null")
                        val queuesResult = async { dao.searchQueueNotSuccess() }
                        val queues = queuesResult.await()
                        Logger.log("[checkQueueInCache] Find queue in localDB: $queues")
                        if (queues.isEmpty()) {
                            updateNavigateState(false)
                            onIdle()
                        } else {
                            getQueueToWork()
                        }
                    }
                } else {
                    onIdle()
                    getQueues(fetchFromRemote = true, rsId = rsId)
                }
                checkQueueOnServerAndSync()
                delay(3000L)
            }
        }
    }
    private fun cancelQueue(queueId: Int) {
        Logger.log("[cancelQueue] Cancel Queue ID: ${queueId}")
        viewModelScope.launch {
            dao.clearQueueById(queueId)
            updateQueueState(null)
            resetNavList()
        }
    }
    private fun onFinishQueue() {
        Logger.log("[onFinishQueue] Function")
        updateQueueState(null)
        resetNavList()
    }
    private fun navigateController(nextStep: Boolean) {
        Logger.log("[navigateController] nextStep: $nextStep")
        if (!_deliveryState.value.isNavigate) {
            Logger.log("[navigateController] isNavigate = false, return")
            return
        }
        val queue = _deliveryState.value.queue
        if(queue==null){
            Logger.log("[navigateController] queue = null, return")
            return
        }

//        Log.d(ROS_TAG, "[navigateController]: $queue")
        syncQueueById()
        val state: Int = queue.STATUS_ID
        val pickupPoint = queue.PICKUP_POINT_ID
        val destinationPoint = queue.DESTINATION_POINT_ID

        if (state == 8) {
            buttonEnable(pickupPoint, false)
            buttonEnable(destinationPoint, false)
            cancelQueue(queue.QUEUE_ID)
            return
        }

        if (nextStep) {
            when (state) {
                1 -> {
                    //Update status pending -> pickup
                    queue.STATUS_ID = 2
                    queue.PICKUP_TIME = getDateTimeStr()
                    updateQueue(queue)
                }

                2 -> {
                    queue.STATUS_ID = 3
                    queue.WAIT_PLACE_TIME = getDateTimeStr()
                    buttonEnable(pickupPoint, true)
                    updateQueue(queue)
                }

                3 -> {
                    //Update status pickup -> waitPlace and Waiting confirm
                    //If confirm then Update status waitPlace -> deliver
                    queue.STATUS_ID = 4
                    queue.DELIVER_TIME = getDateTimeStr()
//                    buttonEnable(pickupPoint, false)
                    updateQueue(queue)
                }

                4 -> {
                    //Nav to destination point
                    queue.STATUS_ID = 5
                    queue.WAIT_PICK_TIME = getDateTimeStr()
                    buttonEnable(destinationPoint, true)
                    updateQueue(queue)
                }

                5 -> {
                    //Update status deliver -> waitPick and Waiting confirm
                    if (queue.JOB_TYPE_ID == 4) {
                        queue.STATUS_ID = 7
                        queue.FINISH_TIME = getDateTimeStr()
                    }else if(queue.JOB_TYPE_ID == 2){
                        queue.STATUS_ID = 7
                        queue.FINISH_TIME = getDateTimeStr()
                    }
                    else{
                        if(queue.PRODUCT_TYPE_ID==1){
                            queue.STATUS_ID = 6
                            queue.CHECKING_TIME = getDateTimeStr()
                        }else{
                            queue.STATUS_ID = 7
                            queue.FINISH_TIME = getDateTimeStr()
                        }
                    }
//                    buttonEnable(destinationPoint, false)
                    updateQueue(queue)
//                    onFinishQueue()
                }
            }
        } else {
            when (state) {
                1 -> {

                }

                2 -> {
                    //Nav to pickup point
                    navToPointName(pointIdToPointName(pickupPoint))
                }

                3 -> {
                    //Update status pickup -> waitPlace and Waiting confirm
                    //If confirm then Update status waitPlace -> deliver
                }

                4 -> {
                    //Nav to destination point
                    navToPointName(pointIdToPointName(destinationPoint))
                }

                5 -> {
                    //Update status deliver -> waitPick and Waiting confirm
                }

                6 -> {
                    onFinishQueue()
                }

                7 -> {
                    onFinishQueue()
                }
            }
        }
    }
    private fun onIdle() {
        Logger.log("[onIdle] Function")
        if (_deliveryState.value.specialArea.name != "ChargeZone") {
            Logger.log("[onIdle] Area != ChargeZone")
            if (shouldSendCmdNav("charging_pile")) {
                navToPointName("charging_pile")
            }
        } else {
            Logger.log("[onIdle] Area = ChargeZone")
            if (_deliveryState.value.coreData.chargingStatus != 2) {
                if (shouldSendCmdNav("charging_pile")) {
                    Logger.log("[onIdle] ChargingStatus !=2, nav to charging_pile")
                    controller?.navigationByPoint("charging_pile")
                }
            }
        }
    }
    private fun buttonEnable(buttonId: Int, state: Boolean) {
        val buttonState = _deliveryState.value.buttonState
        buttonState[buttonId] = state
        _deliveryState.update { it.copy(buttonState = buttonState) }
        Logger.log("[buttonEnable] Update Button State: btnId=${buttonId}, state=${state}")
        Logger.log("[buttonEnable] Update Button State: PRE-> $buttonState")
        Logger.log("[buttonEnable] Update Button State: AFTER-> $buttonState")
    }
    private fun onNavResult(data: String) {
//        Log.d(ROS_TAG, data)
        Logger.log("[onNavResult] Function")
        val parseNavResult = parseResultStr("nav_result", data)
        val navResult = NavResult(
            state = parseNavResult[0].toInt(),
            code = parseNavResult[1].toInt(),
            name = parseNavResult[2],
            distToGoal = parseNavResult[3].toDouble(),
            mileage = parseNavResult[4].toDouble()
        )
        Logger.log("[onNavResult] $navResult")
        _deliveryState.update {
            it.copy(navResult = navResult)
        }

        if (navResult.state == 1) {
            if (!_deliveryState.value.isPlayMusic) {
                updateIsPlayMusic(true)
            }
        } else {
            if (_deliveryState.value.isPlayMusic) {
                updateIsPlayMusic(false)
            }
        }


        when (navResult.state) {
            0 -> {
                //Initial state
                updateRobotMsg("Initial state")
            }

            1 -> {
                // Starts to navigate
                updateRobotMsg("Navigating to ${navResult.name}, distToGoal: ${navResult.distToGoal}, mileage: ${navResult.mileage}")

            }

            2 -> {
                // Pause
                when (navResult.code) {
                    0 -> {
                        //success
                        updateRobotMsg("Navigate is pause")
                    }

                    -1 -> {
                        //failed
                    }
                }
            }

            3 -> {
                //Navigation is complete
                when (navResult.code) {
                    0 -> {
                        //success
                        onNavSuccess(navResult.name)
                        updateRobotMsg("Navigation is complete")
//                        onNavSuccess(navResult.name)
                    }

                    -1 -> {
                        //failed
                    }
                }
            }

            4 -> {
                //Cancellation of navigation
                when (navResult.code) {
                    0 -> {
                        //success
                        updateRobotMsg("Cancellation of navigation")
                    }

                    -1 -> {
                        //failed
                    }
                }
            }

            5 -> {
                //Navigation recovery
                updateRobotMsg("Navigation recovery")
            }

            6 -> {
                // Send cmd success but not start nav
                when (navResult.code) {
                    0 -> {
                        //Success
                        updateRobotMsg("Send cmd nav success")
                    }

                    -1 -> {
                        //docking charging pile
                        updateRobotMsg("Docking charging pile")
                    }

                    -2 -> {
                        //emergency stop switch is pressed
                        updateRobotMsg("Emergency stop switch is pressed")
                    }

                    -3 -> {
                        //The adapter is charging
                    }

                    -4 -> {
                        //target point not found
                        updateRobotMsg("Target point not found")
                    }

                    -5 -> {
                        //AGV docking failed
                        updateRobotMsg("AGV docking failed")
                    }

                    -6 -> {
                        //Abnormal positioning
                        updateRobotMsg("Abnormal positioning")
                    }

                    -7 -> {
                        //The distance between fixed route points is too large
                        updateRobotMsg("The distance between fixed route points is too large")
                    }

                    -8 -> {
                        //No fixed route found
                        updateRobotMsg("No fixed route found")
                    }

                    -9 -> {
                        //Failed to read point information
                        updateRobotMsg("Failed to read point information")
                    }
                }
            }
        }
    }
    private fun onCoreData(data: String) {
        Logger.log("[onCoreData] Function")
        val parseCoreData = parseResultStr("core_data", data)
        val newCoreData = CoreData(
            collision = parseCoreData[0].toInt(),
            antiDrop = parseCoreData[1].toInt(),
            emergencyStop = parseCoreData[2].toInt(),
            power = parseCoreData[3].toInt(),
            chargingStatus = parseCoreData[4].toInt()
        )
        if (newCoreData.chargingStatus == 2 && _deliveryState.value.coreData.chargingStatus != 2) {
            Logger.log("[onCoreData] Nav to charging_pile")
            controller?.relocByName("charging_pile")
        }
        if (newCoreData != _deliveryState.value.coreData) {
            Logger.log("[onCoreData] $newCoreData")
            updateCoreData(newCoreData)
            sendDataToServer(data)
        }
    }
    private fun onNavSuccess(pointName: String) {
        Logger.log("[onNavSuccess] Function")
        Logger.log("[onNavSuccess] NavList: ${_deliveryState.value.navListPoint}")
        if (_deliveryState.value.navListPoint.listPointName.isEmpty() && _deliveryState.value.navListPoint.lastIndex == 0) {
            Logger.log("[onNavSuccess] NavList OFF")
            val statusId = _deliveryState.value.queue?.STATUS_ID
            statusId?.let {
                when (statusId) {
                    2 -> {
                        Logger.log("[onNavSuccess] nav success at statusId=2")
                        val pickupId = _deliveryState.value.queue?.PICKUP_POINT_ID
                        pickupId?.let {
                            if (pointIdToPointName(pickupId) == pointName)
                                navigateController(true)
                        }
                    }

                    4 -> {
                        Logger.log("[onNavSuccess] nav success at statusId=4")
                        val desId = _deliveryState.value.queue?.DESTINATION_POINT_ID
                        desId?.let {
                            if (pointIdToPointName(desId) == pointName)
                                navigateController(true)
                        }
                    }

                    else -> {
                        Logger.log("[onNavSuccess] nav success")
                        navigateController(false)
                    }
                }
            }
        } else {
            Logger.log("[onNavSuccess] NavList ON")
            if (_deliveryState.value.navListPoint.lastIndex == _deliveryState.value.navListPoint.listPointName.size - 1) {
                Logger.log("[onNavSuccess] NavList Success")
                resetNavList()
                navigateController(true)
            } else {
                Logger.log("[onNavSuccess] NavList working")
                if (_deliveryState.value.navListPoint.listPointName[_deliveryState.value.navListPoint.lastIndex] == pointName) {
                    updateNavList(
                        NavListState(
                            listPointName = _deliveryState.value.navListPoint.listPointName,
                            lastIndex = _deliveryState.value.navListPoint.lastIndex + 1
                        )
                    )
                }
                navigateController(false)
            }
        }
    }
    /*
    private fun navToPointName(pointName: String) {
        Logger.log("[navToPointName] Function")
        val specialArea = _deliveryState.value.specialArea
        Logger.log("[navToPointName] navListState: ${_deliveryState.value.navListPoint}")
        Logger.log("[navToPointName] Area: ${_deliveryState.value.specialArea}")
        if (pointName == "Lab") {
            Logger.log("[navToPointName] Target point is Lab")
            if (specialArea.name == "Lab") {
                Logger.log("[navToPointName] Current is in Lab")
                resetNavList()
                if (shouldSendCmdNav(pointName)) {
                    Logger.log("[navToPointName] nav to $pointName")
                    controller?.navigationByPoint(pointName)
                }
            } else {
                Logger.log("[navToPointName] Current is out lab")
                if (_deliveryState.value.navListPoint.listPointName.isEmpty()) {
                    navToListPoint(listOf("FrontDoorLab", pointName))
                } else {
                    val currentPointName =
                        _deliveryState.value.navListPoint.listPointName[_deliveryState.value.navListPoint.lastIndex]
                    if (shouldSendCmdNav(currentPointName)) {
                        navToListPoint(listOf("FrontDoorLab", pointName))
                    }
                }
            }

        } else {
            Logger.log("[navToPointName] Target point isn't Lab")
            if (specialArea.name == "Lab") {
                if (_deliveryState.value.navListPoint.listPointName.isEmpty()) {
                    navToListPoint(listOf("BackDoorLab", "OutLab", pointName))
                } else {
                    val currentPointName =
                        _deliveryState.value.navListPoint.listPointName[_deliveryState.value.navListPoint.lastIndex]
                    if (shouldSendCmdNav(currentPointName)) {
                        navToListPoint(listOf("BackDoorLab", "OutLab", pointName))
                    }
                }
            }
            else {
                resetNavList()
                if (shouldSendCmdNav(pointName)) {
                    controller?.navigationByPoint(pointName)
                }
            }
        }
    }
*/
    private fun navToPointName(pointName: String) {
        Logger.log("[navToPointName] Function")
        val specialArea = _deliveryState.value.specialArea
        Logger.log("[navToPointName] navListState: ${_deliveryState.value.navListPoint}")
        Logger.log("[navToPointName] Area: ${_deliveryState.value.specialArea}")
        if(_deliveryState.value.navListPoint.listPointName.isEmpty()){
            Logger.log("[navToPointName] NavListPoint is empty")
            if(pointName == "Lab"){
                Logger.log("[navToPointName] Target point is Lab")
                if (specialArea.name == "Lab") {
                    Logger.log("[navToPointName] Current is in Lab")
//                    resetNavList()
                    if (shouldSendCmdNav(pointName)) {
                        Logger.log("[navToPointName] nav to $pointName")
                        controller?.navigationByPoint(pointName)
                    }
                } else {
                    Logger.log("[navToPointName] Current is out lab")
                    navToListPoint(listOf("FrontDoorLab", pointName))
                }
            }else{
                Logger.log("[navToPointName] Target point is $pointName")
                if (specialArea.name == "Lab") {
                    Logger.log("[navToPointName] Current is in Lab")
                    navToListPoint(listOf("BackDoorLab", "OutLab",pointName))
                }else{
                    Logger.log("[navToPointName] Current is out lab")
                    if (shouldSendCmdNav(pointName)) {
                        Logger.log("[navToPointName] nav to $pointName")
                        controller?.navigationByPoint(pointName)
                    }
                }
            }
        }else{
            Logger.log("[navToPointName] NavListPoint isn't empty")
            val currentPointName =
                _deliveryState.value.navListPoint.listPointName[_deliveryState.value.navListPoint.lastIndex]
            Logger.log("[navToPointName] NavListPoint current point: $currentPointName")
            if (shouldSendCmdNav(currentPointName)) {
                navToListPoint(listOf("FrontDoorLab", pointName))
            }
        }
    }

    private fun navToListPoint(listPointName: List<String>) {
        Logger.log("[navToListPoint] listPoint: $listPointName")
        if (_deliveryState.value.navListPoint.listPointName.isEmpty()) {
            val tempNavListState = NavListState(
                listPointName = listPointName,
                lastIndex = 0
            )
            updateNavList(tempNavListState)
        }
        val curPoint = _deliveryState.value.navListPoint.listPointName[_deliveryState.value.navListPoint.lastIndex]
        Logger.log("[navToListPoint] NavList current nav to $curPoint")
        controller?.navigationByPoint(curPoint)
    }
    private fun shouldSendCmdNav(pointName: String): Boolean {
        Logger.log("[shouldSendCmdNav] PointName: $pointName")
        val navResult = _deliveryState.value.navResult
        val coreData = _deliveryState.value.coreData
        when (navResult.state) {
            0 -> {
                //Initial state
                Logger.log("[shouldSendCmdNav] Initial state, return true")
                return true
            }

            1 -> {
                // Starts to navigate
                Logger.log("[shouldSendCmdNav] Starts to navigate, return true")
                if (navResult.name != pointName) return true
            }

            2 -> {
                // Pause
                when (navResult.code) {
                    0 -> {
                        //success
                    }

                    -1 -> {
                        //failed
                    }
                }
            }

            3 -> {
                //Navigation is complete
                when (navResult.code) {
                    0 -> {

                    }

                    -1 -> {
                        //failed
                    }
                }
            }

            4 -> {
                //Cancellation of navigation
                when (navResult.code) {
                    0 -> {
                        //success
                    }

                    -1 -> {
                        //failed
                    }
                }
            }

            5 -> {
                //Navigation recovery
            }

            6 -> {
                // Send cmd success but not start nav
                when (navResult.code) {
                    0 -> {
                        //Success
                    }

                    -1 -> {
                        //docking charging pile
                        Logger.log("[shouldSendCmdNav] docking charging pile, return true")
                        if (coreData.chargingStatus != 8) return true
                    }

                    -2 -> {
                        //emergency stop switch is pressed
                        Logger.log("[shouldSendCmdNav] emergency stop switch is pressed, return true")
                        if (coreData.emergencyStop == 1) return true
                    }

                    -3 -> {
                        //The adapter is charging
                    }

                    -4 -> {
                        //target point not found
                    }

                    -5 -> {
                        //AGV docking failed
                    }

                    -6 -> {
                        //Abnormal positioning
                    }

                    -7 -> {
                        //The distance between fixed route points is too large
                    }

                    -8 -> {
                        //No fixed route found
                    }

                    -9 -> {
                        //Failed to read point information
                    }
                }
            }

        }
        Logger.log("[shouldSendCmdNav] Nav Code=${navResult.code}, return false")
        return false
    }

    //Ros
    override fun onResult(result: String?) {
        if (result != null) {

            if (result.startsWith("nav_result")) {
                onNavResult(result)
                sendDataToServer(result)
            }
//            else if (result.startsWith("sys:boot")) {
//
//            }
            else if (result.startsWith("core_data")) {
                onCoreData(result)
            }
//            else if (result.startsWith("move_status")) {
//
//            }
//            else if (result.startsWith("base_vel")) {
//
//            }
//            else if (result.startsWith("nav:pose")) {
//
//            }
            else if (result.startsWith("in_polygon")) {
                onSpecialArea(result)
                sendDataToServer(result)
            }
//            else if (result.startsWith("answer_nearest")) {
//
//            }
            else {
//                Log.i(TAG, "ROS Other: $result")
                sendDataToServer(result)
            }
        }
    }
    private fun onSpecialArea(data: String) {
        Logger.log("[onSpecialArea] Function")
//        Log.d(TAG, data)
        val parseSpecialArea = data
            .replace("in_polygon:", "")
            .split(",")
//        Log.d(TAG, "${parseSpecialArea}, ${parseSpecialArea.size}")
        if (parseSpecialArea.size != 1) {
//            Log.d(TAG, "!=0")
            val tempSpecialArea = SpecialArea(
                name = parseSpecialArea[0],
                type = parseSpecialArea[1].toInt(),
                speed = parseSpecialArea[2].toDouble()
            )
            updateSpecialArea(tempSpecialArea)
        } else {
//            Log.d(TAG, "==0")
            val tempSpecialArea = SpecialArea(
                name = "",
                type = 0,
                speed = -1.0
            )
            updateSpecialArea(tempSpecialArea)
        }
    }
    private fun heartBeats() {
        controller?.heartBeat()
    }
    fun initController(context: Context) {
        Logger.log("[initController] Function")
        if (controller == null) {
            controller = RobotActionController.getInstance()
            try {
                controller?.init(context, "ros_demo", this)
                _deliveryState.update {
                    it.copy(isRosConnected = true, robotMsg = "Init success")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _deliveryState.update {
                    it.copy(isRosConnected = false, robotMsg = "Init error")
                }
            }
        }
    }
    private fun stopListen() {
        Logger.log("[stopListen] Function")
        controller?.stopListen()
        controller = null
    }

    //Socket IO
    private fun listenServer() {
        Logger.log("[listenServer] Function")
        viewModelScope.launch {
            socketRepository.connect()
            socketRepository.listenForEvent("cmd") { args ->
                val data = args[0].toString()
                if (data.startsWith("ros:")) {
                    val cmd = data.replace("ros:", "")
                    controller?.sendCommand(cmd)
                } else if (data.startsWith("localDB:")) {
                    val cmd = data.replace("localDB:", "")
                    if (cmd.startsWith("getQueueAll")) {
                        viewModelScope.launch {
                            socketRepository.emitEvent("report", dao.searchQueue())
                        }
                    } else if (cmd.startsWith("delete")) {
                        val id = cmd.replace("delete[", "")
                            .replace("]", "").toInt()
                        viewModelScope.launch {
                            dao.clearQueueById(id)
                        }
                    }
                }

            }
        }
    }
    private fun socketDisconnect() {
        Logger.log("[socketDisconnect] Function")
        viewModelScope.launch {
            socketRepository.disconnect()
        }
    }
    private fun sendDataToServer(data: String) {

        viewModelScope.launch {
            socketRepository.emitEvent("report", data)
        }
    }

    //Update State
    private fun updateQueueState(newQueue: Queue?){
        Logger.log("[updateQueueState] newQueueState: $newQueue")
        _deliveryState.update { it.copy(queue = newQueue) }
    }
    private fun updateRobotMsg(msg: String) {
        Logger.log("[updateRobotMsg] msg: $msg")
        _deliveryState.update {
            it.copy(robotMsg = msg)
        }
    }
    private fun updateIsPlayMusic(play:Boolean){
        Logger.log("[updateIsPlayMusic] play: $play")
        _deliveryState.update { it.copy(isPlayMusic = play) }
    }
    private fun  updateCoreData(data: CoreData){
        _deliveryState.update { it.copy(coreData = data) }
    }
    private fun resetNavList() {
        Logger.log("[resetNavList] Pre-> ${_deliveryState.value.navListPoint}")
        updateNavList(NavListState(
            listPointName = emptyList(),
            lastIndex = 0
        ))
    }
    private fun updateNavList(data: NavListState){
        Logger.log("[updateNavList] Pre-> ${_deliveryState.value.navListPoint}")
        Logger.log("[updateNavList] New-> $data")
        _deliveryState.update {
            it.copy(
                navListPoint = data
            )
        }
    }
    private fun updateSpecialArea(data: SpecialArea){
        _deliveryState.update {
            it.copy(specialArea = data)
        }
    }

    private fun updateNavigateState(state: Boolean){
        _deliveryState.update {
            it.copy(isNavigate = state)
        }
    }
}