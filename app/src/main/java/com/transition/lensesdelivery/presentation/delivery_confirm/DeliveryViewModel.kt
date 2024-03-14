package com.transition.lensesdelivery.presentation.delivery_confirm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reeman.ros.controller.RobotActionController
import com.reeman.ros.listen.RosCallBackListener
import com.transition.lensesdelivery.data.local.QueueDatabase
import com.transition.lensesdelivery.data.local.QueueEntity
import com.transition.lensesdelivery.data.mapper.toQueue
import com.transition.lensesdelivery.data.mapper.toQueueDetail
import com.transition.lensesdelivery.data.mapper.toQueueEntity
import com.transition.lensesdelivery.domain.model.Queue
import com.transition.lensesdelivery.domain.model.QueueDetail
import com.transition.lensesdelivery.domain.repository.QueueRepository
import com.transition.lensesdelivery.domain.repository.SocketRepository
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

    private var searchJob: Job? = null
    private var navigateJob: Job? = null

    private var controller: RobotActionController? = null

    private val rsId: Int = 1

    private val dao = db.dao

    init {
        resetDBAndNavList()
        listenServer()
    }

    private fun resetDBAndNavList() {
        _deliveryState.update {
            it.copy(
                navListPoint = NavListState(
                    listPointName = emptyList(),
                    lastIndex = 0
                )
            )
        }
        viewModelScope.launch {
            dao.clearQueue()
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

            is QueueEvent.OnSearchQueryChange -> {
                _deliveryState.update {
                    it.copy(searchQuery = event.query)
                }
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500L)
                    getQueues(rsId = rsId)
                }
            }

            is QueueEvent.OnConfirm -> {
                buttonEnable(event.buttonId, false)
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
        viewModelScope.launch {
            repository
                .getQueuesFlow(fetchFromRemote, rsId)
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            result.data?.let { listings ->
                                if (listings.isNotEmpty()) {
                                    getQueueToWork()
                                } else {
                                    _deliveryState.update { it.copy(queueDetail = null) }
                                }
                                _deliveryState.update { it.copy(isConnected = true) }
                            }
                        }

                        is Resource.Error -> {
                            _deliveryState.update { it.copy(isConnected = false) }
                        }

                        is Resource.Loading -> {
                            _deliveryState.update { it.copy(isLoading = result.isLoading) }
                        }
                    }
                }
        }
    }

    //select queue from local db to work
    private fun getQueueToWork() {
//        Log.d(TAG, "getQueueToWork")
        viewModelScope.launch {
            val queue: QueueEntity? = dao.searchQueueOne()
            if (queue != null) {
                _deliveryState.update {
                    it.copy(
                        queue = queue.toQueue(),
                        queueDetail = queue.toQueue().toQueueDetail(),
                        isNavigate = true,
                        navListPoint = NavListState(
                            listPointName = emptyList(),
                            lastIndex = 0
                        )
                    )
                }
                if (queue.toQueue().STATUS_ID == 1) {
                    navigateController(true)
                } else {
                    navigateController(false)
                }
            }
        }
    }

    //check queue success(status_id=6,7) then sync to server and delete from local db
    private fun checkQueueOnServerAndSync() {
//        Log.d(TAG, "checkQueueOnServerAndSync")
        viewModelScope.launch {
            _deliveryState.update { it.copy(isLoading = true) }
            val queuesEntity = dao.searchQueueSuccess()
            if (queuesEntity.isEmpty()) return@launch
            val queues = queuesEntity.map { it.toQueue() }
            for (queue in queues) {

                val updateQueueResult = async {
                    repository
                        .updateQueue(queue)
                }

                when (val result = updateQueueResult.await()) {
                    is Resource.Success -> {
//                        Log.d(TAG, "Update queue to server success")
                        _deliveryState.update { it.copy(isConnected = true) }
                        dao.clearQueueById(queue.QUEUE_ID)
                    }

                    is Resource.Error -> {
//                        Log.d(TAG, "Update queue to server failed")
                        _deliveryState.update { it.copy(isConnected = false) }
                        continue
                    }
                    else -> Unit
                }
            }
        }
    }

    //check queue in server and local if cancel in server then update in local
    private fun syncQueueById() {
//        Log.d(TAG, "syncQueueById")
        viewModelScope.launch {
            _deliveryState.update { it.copy(isLoading = true) }
            if (_deliveryState.value.queue != null) {
                val queueByIdResult = async {
                    repository
                        .getQueueById(_deliveryState.value.queue!!.QUEUE_ID)
                }
                when (val result = queueByIdResult.await()) {
                    is Resource.Success -> {
                        result.data?.let {
                            if (result.data.STATUS_ID == 8) {
                                val localQueue = _deliveryState.value.queue
                                localQueue?.let {
                                    localQueue.STATUS_ID = result.data.STATUS_ID
                                    _deliveryState.update {
                                        it.copy(
                                            queue = localQueue
                                        )
                                    }
                                    dao.updateQueue(localQueue.toQueueEntity())
                                }
                            }
                        }

                        Log.d(TAG, "Sync queue success")
                        _deliveryState.update { it.copy(isLoading = false, isConnected = true) }
                    }

                    is Resource.Error -> {
                        Log.d(TAG, "Sync queue failed")
                        _deliveryState.update { it.copy(isLoading = false, isConnected = false) }
                    }

                    else -> Unit
                }
            }

        }
    }

    //    private fun updateQueueState(queue: Queue){
//        viewModelScope.launch {
//            _deliveryState.update { it.copy(queue = queue) }
//            dao.updateQueue(queue.toQueueEntity())
//        }
//    }
    //Update queue to server
    private fun updateQueue(queue: Queue) {
        viewModelScope.launch {

            _deliveryState.update { it.copy(queue = queue, isLoading = true) }
            dao.updateQueue(queue.toQueueEntity())

            val updateQueueResult = async {
                repository
                    .updateQueue(queue)
            }
            when (val result = updateQueueResult.await()) {
                is Resource.Success -> {
//                    Log.d(TAG, "Update queue to server success")
//                    Log.d(TAG, "${result.data}")
//                    _deliveryState.update { it.copy(isConnected = true) }
                    _deliveryState.update { it.copy(isLoading = false, isConnected = true) }
                }

                is Resource.Error -> {
//                    Log.d(TAG, "Update queue to server failed")
                    _deliveryState.update { it.copy(isLoading = false, isConnected = false) }
                }

                else -> Unit
            }
        }
    }

    // loop check queue in local
    fun checkQueueInCache() {
//        Log.d(TAG, "checkQueueInCache")
        navigateJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                onRosEvent(RosEvent.GetSpecialArea)
//                Log.d(TAG, "${dao.searchQueue()}")
//                Log.d(TAG, "Queue state: ${_deliveryState.value.queue}")
                sendDataToServer("localQueue: ${dao.searchQueue()}")
                sendDataToServer("RobotState: ${_deliveryState.value}")
                if (_deliveryState.value.isNavigate) {
                    if (_deliveryState.value.queue != null) {
                        navigateController(false)
                    } else {
                        val queuesResult = async { dao.searchQueueNotSuccess() }
                        val queues = queuesResult.await()
                        if (queues.isEmpty()) {
                            _deliveryState.update { it.copy(isNavigate = false) }
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
        viewModelScope.launch {
            dao.clearQueueById(queueId)
            _deliveryState.update {
                it.copy(
                    queue = null,
                    queueDetail = null,
                    navListPoint = NavListState(
                        listPointName = emptyList(),
                        lastIndex = 0
                    )
                )
            }
        }
    }

    private fun onFinishQueue() {
//        Log.d(TAG, "onFinish: ${_deliveryState.value.queue}")
        _deliveryState.update {
            it.copy(
                queue = null,
                queueDetail = null,
                navListPoint = NavListState(
                    listPointName = emptyList(),
                    lastIndex = 0
                )
            )
        }
//        Log.d(TAG, "onFinish: ${_deliveryState.value.queue}")
    }

    private fun navigateController(nextStep: Boolean) {
        if (!_deliveryState.value.isNavigate) {
            return
        }
        val queue = _deliveryState.value.queue ?: return
        syncQueueById()
        val state: Int = queue.STATUS_ID
        val pickupPoint = queue.PICKUP_POINT_ID
        val destinationPoint = queue.DESTINATION_POINT_ID

        if (state == 8) {
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
                    } else {
                        queue.STATUS_ID = 6
                        queue.CHECKING_TIME = getDateTimeStr()
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
        updateTaskDetail(queue.toQueueDetail())
    }

    private fun updateTaskDetail(newQueueDetail: QueueDetail) {
        _deliveryState.update { it.copy(queueDetail = newQueueDetail) }
    }

    private fun onIdle() {
        if (_deliveryState.value.specialArea.name != "ChargeZone") {
            if (shouldSendCmdNav("charging_pile")) {
                navToPointName("charging_pile")
            }
        } else {
            if (_deliveryState.value.coreData.chargingStatus != 2) {
                if (shouldSendCmdNav("charging_pile")) {
                    controller?.navigationByPoint("charging_pile")
                }
            }
        }
    }

    private fun buttonEnable(buttonId: Int, state: Boolean) {
        val buttonState = _deliveryState.value.buttonState
        buttonState[buttonId] = state
        _deliveryState.update { it.copy(buttonState = buttonState) }
    }

    private fun showRobotStatus(msg: String) {
        _deliveryState.update {
            it.copy(robotMsg = msg)
        }
    }

    private fun onNavResult(data: String) {
        Log.d(ROS_TAG, data)
        val parseNavResult = parseResultStr("nav_result", data)
        val navResult = NavResult(
            state = parseNavResult[0].toInt(),
            code = parseNavResult[1].toInt(),
            name = parseNavResult[2],
            distToGoal = parseNavResult[3].toDouble(),
            mileage = parseNavResult[4].toDouble()
        )
        _deliveryState.update {
            it.copy(navResult = navResult)
        }

        if (navResult.state == 1) {
            if (!_deliveryState.value.isPlayMusic) {
                _deliveryState.update { it.copy(isPlayMusic = true) }
            }
        } else {
            if (_deliveryState.value.isPlayMusic) {
                _deliveryState.update { it.copy(isPlayMusic = false) }
            }
        }


        when (navResult.state) {
            0 -> {
                //Initial state
                showRobotStatus("Initial state")
            }

            1 -> {
                // Starts to navigate
                showRobotStatus("Navigating to ${navResult.name}, distToGoal: ${navResult.distToGoal}, mileage: ${navResult.mileage}")

            }

            2 -> {
                // Pause
                when (navResult.code) {
                    0 -> {
                        //success
                        showRobotStatus("Navigate is pause")
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
                        showRobotStatus("Navigation is complete")
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
                        showRobotStatus("Cancellation of navigation")
                    }

                    -1 -> {
                        //failed
                    }
                }
            }

            5 -> {
                //Navigation recovery
                showRobotStatus("Navigation recovery")
            }

            6 -> {
                // Send cmd success but not start nav
                when (navResult.code) {
                    0 -> {
                        //Success
                        showRobotStatus("Send cmd nav success")
                    }

                    -1 -> {
                        //docking charging pile
                        showRobotStatus("Docking charging pile")
                    }

                    -2 -> {
                        //emergency stop switch is pressed
                        showRobotStatus("Emergency stop switch is pressed")
                    }

                    -3 -> {
                        //The adapter is charging
                    }

                    -4 -> {
                        //target point not found
                        showRobotStatus("Target point not found")
                    }

                    -5 -> {
                        //AGV docking failed
                        showRobotStatus("AGV docking failed")
                    }

                    -6 -> {
                        //Abnormal positioning
                        showRobotStatus("Abnormal positioning")
                    }

                    -7 -> {
                        //The distance between fixed route points is too large
                        showRobotStatus("The distance between fixed route points is too large")
                    }

                    -8 -> {
                        //No fixed route found
                        showRobotStatus("No fixed route found")
                    }

                    -9 -> {
                        //Failed to read point information
                        showRobotStatus("Failed to read point information")
                    }
                }
            }
        }
    }

    private fun onCoreData(data: String) {
        val parseCoreData = parseResultStr("core_data", data)
        val newCoreData = CoreData(
            collision = parseCoreData[0].toInt(),
            antiDrop = parseCoreData[1].toInt(),
            emergencyStop = parseCoreData[2].toInt(),
            power = parseCoreData[3].toInt(),
            chargingStatus = parseCoreData[4].toInt()
        )
        if (newCoreData != _deliveryState.value.coreData) {
            if (newCoreData.chargingStatus == 2 && _deliveryState.value.coreData.chargingStatus != 2) {
                controller?.relocByName("charging_pile")
            }
            _deliveryState.update { it.copy(coreData = newCoreData) }
            sendDataToServer(data)
        }
    }

    private fun onNavSuccess(pointName: String) {
//        Log.d(TAG, "OnNavSuccess")
//        Log.d(TAG, "${_deliveryState.value.navListPoint.listPointName}, ${_deliveryState.value.navListPoint.lastIndex}")
        if (_deliveryState.value.navListPoint.listPointName.isEmpty() && _deliveryState.value.navListPoint.lastIndex == 0) {
            val statusId = _deliveryState.value.queue?.STATUS_ID
            statusId?.let {
                when (statusId) {
                    2 -> {
                        val pickupId = _deliveryState.value.queue?.PICKUP_POINT_ID
                        pickupId?.let {
                            if (pointIdToPointName(pickupId) == pointName)
                                navigateController(true)
                        }
                    }

                    4 -> {
                        val desId = _deliveryState.value.queue?.DESTINATION_POINT_ID
                        desId?.let {
                            if (pointIdToPointName(desId) == pointName)
                                navigateController(true)
                        }
                    }

                    else -> {
                        navigateController(false)
                    }
                }
            }
        } else {
            if (_deliveryState.value.navListPoint.lastIndex == _deliveryState.value.navListPoint.listPointName.size - 1) {
                resetNavList()
                navigateController(true)
            } else {
                if (_deliveryState.value.navListPoint.listPointName[_deliveryState.value.navListPoint.lastIndex] == pointName) {
                    _deliveryState.update {
                        it.copy(
                            navListPoint = NavListState(
                                listPointName = _deliveryState.value.navListPoint.listPointName,
                                lastIndex = _deliveryState.value.navListPoint.lastIndex + 1
                            )
                        )
                    }
                }
                navigateController(false)
            }
        }
    }

    private fun resetNavList() {
        _deliveryState.update {
            it.copy(
                navListPoint = NavListState(
                    listPointName = emptyList(),
                    lastIndex = 0
                )
            )
        }
    }

    private fun navToPointName(pointName: String) {
        Log.d(TAG, "isNavListEmpty: ${_deliveryState.value.navListPoint.listPointName.isEmpty()}")
        Log.d(TAG, "lastIndex: ${_deliveryState.value.navListPoint.lastIndex}")
        val specialArea = _deliveryState.value.specialArea
//        Log.d(TAG, "navToPoint: area=${specialArea.name}")
        if (pointName == "Lab") {
            if (specialArea.name == "Lab") {
                resetNavList()
                if (shouldSendCmdNav(pointName)) {
                    controller?.navigationByPoint(pointName)
                }
            } else {
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
            } else {
                resetNavList()
                if (shouldSendCmdNav(pointName)) {
                    controller?.navigationByPoint(pointName)
                }
            }

        }
    }

    private fun navToListPoint(listPointName: List<String>) {
        if (_deliveryState.value.navListPoint.listPointName.isEmpty()) {
            val tempNavListState = NavListState(
                listPointName = listPointName,
                lastIndex = 0
            )
            _deliveryState.update {
                it.copy(
                    navListPoint = tempNavListState
                )
            }
        }

        controller?.navigationByPoint(_deliveryState.value.navListPoint.listPointName[_deliveryState.value.navListPoint.lastIndex])
    }

    private fun shouldSendCmdNav(pointName: String): Boolean {
        val navResult = _deliveryState.value.navResult
        val coreData = _deliveryState.value.coreData
        when (navResult.state) {
            0 -> {
                //Initial state
                return true
            }

            1 -> {
                // Starts to navigate
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
                        if (coreData.chargingStatus != 8) return true
                    }

                    -2 -> {
                        //emergency stop switch is pressed
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
            _deliveryState.update {
                it.copy(
                    specialArea = tempSpecialArea
                )
            }
        } else {
//            Log.d(TAG, "==0")
            val tempSpecialArea = SpecialArea(
                name = "",
                type = 0,
                speed = -1.0
            )
            _deliveryState.update {
                it.copy(
                    specialArea = tempSpecialArea
                )
            }
        }
    }

    private fun heartBeats() {
        controller?.heartBeat()
//        Log.i(TAG, "HeartBeat")
    }

    fun initController(context: Context) {
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
        controller?.stopListen()
        controller = null
    }

    //Socket IO
    private fun listenServer() {
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
        viewModelScope.launch {
            socketRepository.disconnect()
        }
    }

    private fun sendDataToServer(data: String) {
        viewModelScope.launch {
            socketRepository.emitEvent("report", data)
        }
    }

    //Music
//    private fun addMediaItem(){
//        player.addMediaItem(MediaItem.fromUri(Uri.parse("android.resource://raw/${R.raw.crazy}")))
//    }
}