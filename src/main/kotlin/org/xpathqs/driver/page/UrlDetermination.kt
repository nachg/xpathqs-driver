package org.xpathqs.driver.page

interface UrlDetermination {
    fun isCurrentPage(url: String): Boolean
}