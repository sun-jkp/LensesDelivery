package com.transition.lensesdelivery.presentation.delivery_confirm

import android.content.Context
import com.reeman.ros.controller.RobotActionController
import com.reeman.ros.listen.RosCallBackListener
import javax.inject.Inject
import javax.inject.Singleton


class RosController (private val controller: RobotActionController, private val context: Context): RosCallBackListener {

    fun initController(){
        try{
            controller.init(context, "ros_demo", this)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun stopListen(){
        controller.stopListen()
    }
    override fun onResult(result: String?) {
        TODO("Not yet implemented")
    }
}