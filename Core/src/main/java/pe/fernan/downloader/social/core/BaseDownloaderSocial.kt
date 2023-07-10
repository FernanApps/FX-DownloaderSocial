package pe.fernan.downloader.social.core

import pe.fernan.downloader.social.core.model.DataVideo

abstract class BaseDownloaderSocial {
    abstract val baseUrl: String
    abstract val regex: String
    abstract operator fun get(url: String): DataVideo?
}

/*
    def nameProperty = '_name'
    def mainClassProperty = '_mainClass'
    def authorProperty = '_authors'
    def versionProperty = '_version'
    def descriptionProperty = '_description'
    def iconUrlProperty = '_iconUrl'
 */