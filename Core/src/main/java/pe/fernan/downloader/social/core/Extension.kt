package pe.fernan.downloader.social.core

import com.google.gson.annotations.SerializedName

data class Extension(
    @SerializedName("_authors")
    val authors: String?,
    @SerializedName("_description")
    val description: String?,
    @SerializedName("_iconUrl")
    val iconUrl: String?,
    @SerializedName("internalName")
    val internalName: String?,
    @SerializedName("_mainClass")
    val mainClass: String?,
    @SerializedName("_name")
    val name: String?,
    @SerializedName("_version")
    val version: String?
)