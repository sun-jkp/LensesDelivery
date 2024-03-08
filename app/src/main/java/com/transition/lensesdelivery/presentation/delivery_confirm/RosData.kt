package com.transition.lensesdelivery.presentation.delivery_confirm

data class NavResult(
    val state: Int = 0,
    val code: Int = 0,
    val name: String = "-1",
    val distToGoal: Double = 0.0,
    val mileage: Double = 0.0
)

data class CoreData(
    val collision: Int = 0,
    val antiDrop: Int = 0,
    val emergencyStop: Int = 0,
    val power: Int = 100,
    val chargingStatus: Int = 1
)

data class SpecialArea(
    var name: String = "",
    var type: Int = 0,
    var speed: Double = -1.0,
)

data class NavListState(
    val listPointName: List<String> = emptyList(),
    val lastIndex: Int = 0
)