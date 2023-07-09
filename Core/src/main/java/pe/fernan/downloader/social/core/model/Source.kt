package pe.fernan.downloader.social.core.model

import pe.fernan.downloader.social.core.utils.uniqueId


data class SourceItem(
    val source: Source,
    var download: Download,
)
data class Download(
    val id: String = "",
    var progress: Int = 0,
    var isDownload: Boolean = false

)


data class Source(
    val url: String,
    val title: String,
    val type: Type = Type.MP4,
) {

    // Extension Add
    val id: String get() = url.uniqueId()

    val extension: String get() = "." + this.type.toString().lowercase()

    enum class Type {
        MP3, MP4, SLIDE
    }

    private fun fileName(name: String) = "$name." + when (this.type) {
        Type.MP3 -> "mp3"
        Type.SLIDE -> TODO()
        else -> "mp4"
    }

    override fun toString(): String {
        return "Source(url='$url', title='$title', type=$type)"
    }


}

sealed class DataState <out R>{
    object Initial : DataState<Nothing>()
    object Loading : DataState<Nothing>()
    data class Progress(val progress: Int): DataState<Nothing>(){}
    data class Success<out T>(val data: T): DataState<T>()
    data class Error(val exception: Throwable): DataState<Nothing>()
    object Finished: DataState<Nothing>()
}