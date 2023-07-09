package pe.fernan.downloader.social

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import pe.fernan.downloader.social.constants.Constant
import pe.fernan.downloader.social.ui.ShareActivity


class ShareButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //dumpIntent(intent)
        val fileId = intent.getStringExtra(Constant.FILE_RECEIVER_ID)!!
        val notificationId = intent.getIntExtra(Constant.NOTIFICATION_RECEIVER_ID,-1)!!


//        context.applicationContext.startActivity(Intent(context.applicationContext, ShareActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            putExtra(Constant.NOTIFICATION_RECEIVER_ID, fileId)
//        })

        val shareIntent = Intent(context.applicationContext, ShareActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Constant.FILE_RECEIVER_ID, fileId)
        }

        val sharePendingIntent = PendingIntent.getActivity(
            context.applicationContext,
            0,
            shareIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        try {
            sharePendingIntent.send()
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }



        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)

    }
}

fun dumpIntent(i: Intent) {
    val bundle = i.extras
    if (bundle != null) {
        val keys = bundle.keySet()
        val it: Iterator<String> = keys.iterator()
        Log.e("LOG_TAG", "Dumping Intent start")
        while (it.hasNext()) {
            val key = it.next()
            Log.e("LOG_TAG", "[" + key + "=" + bundle[key] + "]")
        }
        Log.e("LOG_TAG", "Dumping Intent end")
    }
}

