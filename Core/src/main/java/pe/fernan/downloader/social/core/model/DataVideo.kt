package pe.fernan.downloader.social.core.model

import pe.fernan.downloader.social.core.utils.uniqueId

data class DataVideo(
    val socialUrl: String,
    val author: Author,
    val description: String,
    val sources: List<Source>,
) {

    val id get() = socialUrl.uniqueId()

    override fun toString(): String {
        return "DataVideo" +
                "\n(" +
                "\ntiktokUrl='$socialUrl'," +
                "\nauthor=$author, " +
                "\ndescription='$description'" +
                "\n)"
    }
}



