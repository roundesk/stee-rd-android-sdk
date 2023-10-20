package com.roundesk.sdk.util

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

class SaveLogsToFile(
    context: Context
) {
    private val pId = android.os.Process.myPid()
    private var logcatProcess: Process? = null
    private var reader: BufferedReader? = null
    private var isRunning = true
    private val command = "logcat | grep $pId"
    private var outputStream: FileOutputStream? = null

    private val cDir: File? = context.getExternalFilesDir(null);
    private val appDirectory = File(cDir?.path + "/" + "STEE_APP_DATA_LOGS")
    private val logDirectory = File("$appDirectory/logs")


    //    val date = System.currentTimeMillis()
    init {
        if (!appDirectory.exists()) {
            appDirectory.mkdir()
        }

        // create log folder
        if (!logDirectory.exists()) {
            logDirectory.mkdir()
        }
    }


    suspend fun startLog(className: String) {
         var logFile = File(logDirectory, "logcat_$className" + System.currentTimeMillis() + ".txt")
        if (isExternalStorageWritable()) {

                outputStream = withContext(Dispatchers.IO) {
                    FileOutputStream(logFile)
                }
                    var line = ""
                    try {
                        logcatProcess = Runtime.getRuntime().exec(command)
                        reader =
                            BufferedReader(InputStreamReader(logcatProcess!!.inputStream), 1024)
                        while (isRunning) {
                            line =
                                withContext(Dispatchers.IO) {
                                    reader!!.readLine()
                                }
                        if (!line.isNullOrEmpty() && line.contains("$pId") && checkClockRateString(line)
                        ) {

                            val fileSizeInMb = (logFile.length()/1024) / 1024
                            val logDirectorySize = (logDirectory.length()/1024) /1024

                            if(logDirectory.listFiles()!!.size >=10){
                                deleteFirstFileFromLogDir(logDirectory)
                            }
                            if (fileSizeInMb >= 10) {
                                withContext(Dispatchers.IO) {
                                    outputStream!!.close()
                                }
                                logFile = File(logDirectory, "logcat_$className" + System.currentTimeMillis() + ".txt")
                                outputStream =
                                    withContext(Dispatchers.IO) {
                                        FileOutputStream(logFile)
                                    }
                            }
                            withContext(Dispatchers.IO) {
                                outputStream!!.write((line + System.lineSeparator()).toByteArray())
                            }

                        }
                        }
                    } catch (_: IOException,) {

                    }catch (_: java.lang.NullPointerException){

                    }

                }




    }

    private fun deleteFirstFileFromLogDir(file: File){
        val fileList = file.listFiles()!!.asList().sortedBy { it.lastModified() }
        if(fileList.size > 10){
            fileList[0].delete()
            deleteFirstFileFromLogDir(file)
        }
    }

    private fun checkClockRateString(line: String): Boolean {
        return (!line.contains("clockRate"))
    }

    fun stopLog() {
        isRunning = false
        outputStream!!.close()
    }


    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }


}

