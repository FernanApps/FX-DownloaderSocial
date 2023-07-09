package pe.fernan.downloader.social.core.model

data class Author(
    val name: String = "",
    val avatar: String = "",
) {
    override fun toString(): String {
        return "Author(name='$name', avatar='$avatar')"
    }
}