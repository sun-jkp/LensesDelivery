package com.transition.lensesdelivery.util

fun parseResultStr(prefix: String, data: String): List<String> {
    //Pattern    prefix{0 0 0 0 0} size = n
    return data
        .replace("$prefix{", "")
        .replace("}", "")
        .split(" ")
}


fun pointIdToPointName(pointId: Int): String {
    return when (pointId) {
        0 -> {
            "Lab"
        }

        1 -> {
            "Line:1"
        }

        2 -> {
            "Line:2"
        }

        3 -> {
            "Line:3"
        }

        4 -> {
            "Line:4"
        }

        5 -> {
            "Line:5"
        }

        6 -> {
            "Line:6"
        }

        7 -> {
            "Line:7"
        }

        8 -> {
            "Line:8"
        }

        99 -> {
            "charging_pile"
        }

        else -> {
            "Unknown"
        }
    }
}