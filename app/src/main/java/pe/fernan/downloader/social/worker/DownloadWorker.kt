package pe.fernan.downloader.social.worker

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import pe.fernan.downloader.social.R
import pe.fernan.downloader.social.ShareButtonReceiver
import pe.fernan.downloader.social.constants.Constant
import pe.fernan.downloader.social.constants.Constant.NOTIFICATION_CHANNEL
import pe.fernan.downloader.social.constants.Constant.NOTIFICATION_ID
import pe.fernan.downloader.social.constants.Constant.NOTIFICATION_IMPORTANCE_HIGH
import pe.fernan.downloader.social.constants.Constant.NOTIFICATION_NAME
import pe.fernan.downloader.social.core.utils.uniqueId
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

/*
    According to developer.android document, Kotlin developers should use CoroutineWorker instead of Worker.

    You Can Use Retrofit for Download file But im Using OkHttp For Download video link

    All File Store in internal path (data/data/packageName/cache) Which the User Does not have access to (only Root)

 */
class DownloadWorker(private val context: Context, private val workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    private var authorName = ""
    override suspend fun doWork(): Result {
        Timber.d("work execute")

        // fileName + source.extension
        val fileName = inputData.getString(Constant.FILE_NAME)!!
        val fileUrl = inputData.getString(Constant.FILE_URL)!!
        authorName = inputData.getString(Constant.AUTHOR_NAME) ?: ""
        val downloadUrlId = fileUrl.uniqueId()

        return try {
            setForeground(createForegroundInfo(0))
            //start a delaly before download file
            delay(100L)
            //setProgressAsync(workDataOf("progress" to 0))
            downloadFile(fileName, fileUrl, downloadUrlId)

        } catch (e: Exception) {
            Timber.d("exception ${e.localizedMessage}")
            Result.failure()
        }


    }



    private suspend fun downloadFile(fileName: String, fileUrl: String, downloadUrlId: String): Result {
        val request = Request.Builder()
            .url(fileUrl)
            .build()
        val client = OkHttpClient()

        val response = client.newCall(request).awaitResponse()
        val file = getPathVideo(context, fileName)

        response.body?.let { body ->
            // work with file should be in I/O Thread
            withContext(Dispatchers.IO) {
                val outputStream = FileOutputStream(file)
                val byteStream = body.byteStream()
                byteStream.use { inputStream ->
                    outputStream.use { outputStream ->
                        try {
                            val total = body.contentLength()
                            var progressByte = 0L
                            var bytesSinceLastUpdate = 0
                            val data = ByteArray(8_192)
                            while (true) {
                                val bytes = inputStream.read(data)
                                if (bytes == -1) {
                                    break
                                }
                                outputStream.write(data, 0, bytes)
                                progressByte += bytes
                                bytesSinceLastUpdate += bytes

                                if (bytesSinceLastUpdate >= Constant.UPDATE_INTERVAL_BYTES) {
                                    val percent = ((progressByte * 100) / total).toInt()
                                    setForeground(createForegroundInfo(percent))
                                    setProgressAsync(workDataOf(Constant.PROGRESS to percent, Constant.PROGRESS_ID to downloadUrlId))
                                    bytesSinceLastUpdate = 0
                                }
                            }

                        } catch (e: Exception) {
                            return@withContext Result.failure(
                                workDataOf(Constant.ERROR_File to e.localizedMessage)
                            )
                        }
                    }
                }

            }

            makeStatusNotification(authorName, fileName)
            setProgressAsync(workDataOf(Constant.PROGRESS to 100, Constant.PROGRESS_ID to downloadUrlId))
            return Result.success(workDataOf(Constant.SUCCESS_FILE to file.absolutePath, Constant.SUCCESS_ID to downloadUrlId))
        } ?: kotlin.run {
            return Result.failure(workDataOf(Constant.ERROR_NETWORK to "error in fetch"))
        }

    }


    @SuppressLint("MissingPermission")
    private suspend fun makeStatusNotification(title: String, fileName: String) {
        withContext(Dispatchers.Main){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannel()
            }


            val notificationId = Random.nextInt()
            // Crear una acción para el botón
            val shareIntent = Intent(context.applicationContext, ShareButtonReceiver::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(Constant.FILE_RECEIVER_ID, fileName)
                putExtra(Constant.NOTIFICATION_RECEIVER_ID, notificationId)

            }

//            val _shareIntent = Intent(context.applicationContext, ShareActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                putExtra(Constant.FILE_RECEIVER_ID, fileName)
//                putExtra(Constant.NOTIFICATION_RECEIVER_ID, notificationId)
//            }

            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, shareIntent, 0)

            val sharePendingIntent = PendingIntent.getBroadcast(
                context.applicationContext,
                0,
                shareIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val shareText = "Share"

            val builder = NotificationCompat.Builder(context.applicationContext, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText("Download Finish")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(LongArray(0))
                //.setContentIntent(pendingIntent)
                //.addAction(R.drawable.baseline_share_24, shareText, sharePendingIntent)

            val notificationManager = NotificationManagerCompat.from(context.applicationContext)
            notificationManager.notify(notificationId, builder.build())

        }

    }





    private fun createForegroundInfo(progress: Int): ForegroundInfo {
        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
            .setContentTitle("Downlaod Video")
            .setContentText("downloading... $progress %")
            .setSmallIcon(R.drawable.baseline_file_download_24)
            .setOngoing(true)
            .setProgress(100, progress, false)
            .setSilent(true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel =
            NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, NOTIFICATION_IMPORTANCE_HIGH)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }





    /*
    i write this method because i want to call a suspend method in on response create a suspendCancellableCoroutine

     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend inline fun Call.awaitResponse(): Response {
        return suspendCancellableCoroutine { cancellableContinuation ->
            val call = OkHttpClient().newCall(this.request())
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cancellableContinuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    cancellableContinuation.resume(response) {

                    }
                }
            })
            cancellableContinuation.invokeOnCancellation {
                call.cancel()
            }
        }
    }

    companion object {
        fun getPathVideo(context: Context, fileName: String): File {
            return File(context.cacheDir, fileName)
        }
    }

    /*

    private void sendNotification(
            String id,
            String _type,
            String title,
            String imageUri,
            String iconUrl,
            String message
    ) {


        Bitmap image = getBitmapfromUrl(imageUri);
        Bitmap icon = getBitmapfromUrl(iconUrl);



        Intent intent = new Intent(this, LoadActivity.class);
        intent.setAction(Long.toString(System.currentTimeMillis()));

        intent.putExtra("id", Integer.parseInt(id));
        intent.putExtra("from", "notification");
        intent.putExtra("type", "poster");
        intent.putExtra("_type", _type);


        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        int NOTIFICATION_ID = Integer.parseInt(id);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "my_channel_01";
        CharSequence name = "my_channel";
        String Description = "This is my channel";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)

                //.setSmallIcon(R.drawable.ic_stat_ic_notification);
                .setSmallIcon(R.drawable.ic_logo_notification)
                .setContentTitle(title)
                .setContentText(message)
                //.setColor(ContextCompat.getColor(this, R.color.colorAccent))

                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(message);

        if (icon!=null){
            //builder.setLargeIcon(icon);

        }else{
            //builder.setLargeIcon(largeIcon);
        }
        if (image!=null){
            builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(image));
        } else {
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        }


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(HomeActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        notificationManager.notify(NOTIFICATION_ID, builder.build());


    }



     */


}