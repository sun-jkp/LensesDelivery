package com.transition.lensesdelivery.util

import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {
    private var logFile: File? = null
    private var currentLogFileDate: String? = null

    private fun createLogFileForToday(): File {
        val root = Environment.getExternalStorageDirectory()
        val dir = File(root.absolutePath + "/Logs")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val fileName = "app_log_$currentDate.txt"

        return File(dir, fileName)
    }

    private fun updateLogFile() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        if (currentLogFileDate != currentDate || logFile == null) {
            logFile = createLogFileForToday()
            currentLogFileDate = currentDate
        }
    }

    private fun getCurrentTimeStamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = dateFormat.format(Date())
        return "[$currentTime] "
    }

    fun log(message: String) {
        updateLogFile()
        logFile?.let {
            try {
                val logMessage = "${getCurrentTimeStamp()}$message\n"
                it.appendText(logMessage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
