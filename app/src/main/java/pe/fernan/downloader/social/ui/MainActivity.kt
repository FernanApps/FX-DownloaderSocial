package pe.fernan.downloader.social.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequest
import dagger.hilt.android.AndroidEntryPoint
import pe.fernan.downloader.social.ui.main.MainScreen
import pe.fernan.downloader.social.ui.theme.FernanTikTokDownloaderTheme
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var downloadRequest: OneTimeWorkRequest
    private val notificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { Granted ->
            if (Granted) {
                Timber.d("permission Granted")
                Toast.makeText(this, "Granted, Please Click Start button again", Toast.LENGTH_SHORT)
                    .show()
                // do work here
            } else {
                Timber.d("permission Denied")
            }

        }


    private val viewModel: MainViewModel by viewModels()


    private fun getSharedText(intent: Intent): String? {
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type == "text/plain") {
            return intent.getStringExtra(Intent.EXTRA_TEXT)
        }
        return null
    }

    private fun getSharedTextUrlAndSet(intent: Intent) {
        viewModel.setDownloadUrl(getSharedText(intent))
        viewModel.downloadVideo()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSharedTextUrlAndSet(intent)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            FernanTikTokDownloaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        getSharedTextUrlAndSet(intent)
    }



}









