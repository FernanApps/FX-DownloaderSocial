package pe.fernan.downloader.social.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pe.fernan.downloader.social.constants.Constant
import pe.fernan.downloader.social.core.model.Author
import pe.fernan.downloader.social.core.model.DataState
import pe.fernan.downloader.social.core.model.DataVideo
import pe.fernan.downloader.social.core.model.Download
import pe.fernan.downloader.social.core.model.Source
import pe.fernan.downloader.social.core.model.SourceItem
import pe.fernan.downloader.social.worker.DownloadWorker
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val workManager: WorkManager,
) : ViewModel() {

    private val _downloadUrl = MutableLiveData<String>()
    val downloadUrl: LiveData<String> = _downloadUrl

    private val _dataVideo = MutableLiveData<DataVideo?>()
    val dataVideo: LiveData<DataVideo?> get() = _dataVideo

    private val _loading = MutableLiveData<Boolean?>(null)
    val loading: LiveData<Boolean?> get() = _loading

    private val _downloadStatus = MutableLiveData<DataState<Boolean>?>(null)
    val downloadStatus: LiveData<DataState<Boolean>?> get() = _downloadStatus

    private val _progressDownload = MutableLiveData<Download>(null)
    val progressDownload: LiveData<Download> get() = _progressDownload

    private val _sources = MutableLiveData<List<SourceItem>>(null)
    val sources: LiveData<List<SourceItem>> get() = _sources

    private var sourceItemMap = mutableMapOf<String, SourceItem>()

    //private val repo = TiktokDownloadRepository()


    private val regexSocialList = mutableListOf(
        "http.+downloader.+"
    ).map {
        Regex(it)
    }

    fun setDownloadUrl(url: String?) {
        url ?: return
        run breaking@{
            regexSocialList.forEach {
                if (it.matches(url)) {
                    _downloadUrl.value = url
                    _sources.value = null
                    return@breaking
                }
            }

        }


    }

    fun downloadVideo() {
        if (_downloadUrl.value.isNullOrEmpty()) {
            return
        }
        viewModelScope.launch {
            _loading.value = true
            //val data = repo.downloadPage(_downloadUrl.value!!)
            val data = DataVideo("", Author(),"", listOf())

            println(":data ::::: $data")
            println(":data ::::: ${data?.sources}")
            _dataVideo.value = data
            sourceItemMap = data!!.sources.associate {
                it.id to SourceItem(
                    download = Download(),
                    source = it
                )
            }.toMutableMap()
            println(":data ::::: map ${sourceItemMap.values}}")

            _sources.value = ArrayList(sourceItemMap.values)
            _loading.value = false
        }
    }

    private fun filterItemAndUpdate(sourceId: String?, download: Download) {
        if (sourceId != null) {
            val currentSources = sourceItemMap
            currentSources[sourceId]?.let { item ->
                currentSources[sourceId] =
                    currentSources[sourceId]!!.copy(
                        download = download
                    )
                sourceItemMap = currentSources
                _sources.value = ArrayList(sourceItemMap.values)
            }

        } else {

        }
    }

    fun startWork(authorName:String, fileName: String, source: Source) {

        Timber.d("startWork $fileName")
        Timber.d("startWork $source")

        val inputData = Data.Builder().apply {
            putString(Constant.FILE_NAME, fileName + source.extension)
            putString(Constant.FILE_URL, source.url)
            putString(Constant.AUTHOR_NAME, authorName)

        }.build()


        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .build()


        // for Unique Work
        workManager.enqueueUniqueWork(
            Constant.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            downloadRequest
        )

        workManager.getWorkInfosForUniqueWorkLiveData(Constant.UNIQUE_WORK_NAME)
            .observeForever { workInfos ->
                val downloadWorkInfo = workInfos.find { it.id == downloadRequest.id }
                if (downloadWorkInfo != null) {
                    Timber.d("DOWNLOAD_WORK_INFO " + downloadWorkInfo.toString())
                    Timber.d("DOWNLOAD_WORK_INFO " + downloadWorkInfo.state.toString())
                    Timber.d("DOWNLOAD_WORK_INFO " + downloadWorkInfo.outputData.toString())
                    when (downloadWorkInfo.state) {
                        WorkInfo.State.RUNNING -> {
                            val progress = downloadWorkInfo.progress.getInt(Constant.PROGRESS, 0)
                            val progressId =
                                downloadWorkInfo.progress.getString(Constant.PROGRESS_ID)

                            filterItemAndUpdate(
                                progressId,
                                Download( progress = progress)
                            )


                        }

                        WorkInfo.State.SUCCEEDED -> {
                            val sourceId =
                                downloadWorkInfo.outputData.getString(Constant.SUCCESS_ID)
                            filterItemAndUpdate(
                                sourceId, Download(
                                    isDownload = sourceId != null

                                )
                            )
                        }

                        WorkInfo.State.CANCELLED -> {
                            "CANCELLED Work"
                        }

                        WorkInfo.State.FAILED -> {
//                        val errorNetworkData =
//                            workInfo?.outputData?.getString(Constant.ERROR_NETWORK)
//                        val errorFileData = workInfo?.outputData?.getString(Constant.ERROR_File)
//                        val errorUrl = workInfo?.outputData?.getString(Constant.ERROR_FILE_URL)
//                        "CANCELLED Work $errorUrl\nNetwork $errorNetworkData \nFile $errorFileData"

                        }

                        WorkInfo.State.ENQUEUED -> {

                        }
                        else -> {
                            "unknown error"
                        }

                    }


                }


            }

    }


}









