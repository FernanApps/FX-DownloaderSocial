package pe.fernan.downloader.social.constants

import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi

object Constant  {

    const val AUTHOR_NAME = "author_name"
    const val UNIQUE_WORK_NAME = "work_download"
    const val FILE_NAME = "file_name"
    const val FILE_URL = "file_url_id"
    const val ERROR_NETWORK = "error_network"
    const val ERROR_File = "error_file"
    const val SUCCESS_FILE = "Success_file"
    const val SUCCESS_ID = "Success_url"
    const val ERROR_FILE_URL = "error_file_url"

    const val PROGRESS_ID = "progress_id"
    const val PROGRESS = "progress"

    const val FILE_RECEIVER_ID = "file_receiver_id"
    const val NOTIFICATION_RECEIVER_ID = "notification_receiver_id"

    const val unknownError = "error_unknow"


    const val NOTIFICATION_CHANNEL = "fernan_download_channel"
    const val NOTIFICATION_NAME = "File Download"
    @RequiresApi(Build.VERSION_CODES.N)
    const val NOTIFICATION_IMPORTANCE_HIGH = NotificationManager.IMPORTANCE_HIGH

    const val NOTIFICATION_ID = 14


    const val UPDATE_INTERVAL_BYTES = 1024

}

