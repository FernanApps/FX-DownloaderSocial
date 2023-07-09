package pe.fernan.downloader.social.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pe.fernan.downloader.social.constants.Constant
import pe.fernan.downloader.social.dumpIntent
import pe.fernan.downloader.social.ui.theme.FernanTikTokDownloaderTheme
import pe.fernan.downloader.social.utils.ShareUtils
import pe.fernan.downloader.social.worker.DownloadWorker

class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dumpIntent(intent)

        val fileId = intent.getStringExtra(Constant.NOTIFICATION_RECEIVER_ID)
        if(fileId != null){
            ShareUtils.videoTo(
                videoFile = DownloadWorker.getPathVideo(
                    this, fileId
                ),
                context = this
            )
        }
        //finish()

        setContent {
            FernanTikTokDownloaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }


    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FernanTikTokDownloaderTheme {
        Greeting("Android")
    }
}