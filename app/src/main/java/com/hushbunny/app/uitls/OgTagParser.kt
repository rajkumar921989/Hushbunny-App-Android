package com.hushbunny.app.uitls

import com.hushbunny.app.ui.model.LinkSourceContent
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


const val OG_TITLE: String = "og:title"
const val OG_DESCRIPTION: String = "og:description"
const val OG_TYPE: String = "og:type"
const val OG_IMAGE: String = "og:image"
const val OG_URL: String = "og:url"
const val OG_SITE_NAME: String = "og:site_name"

class OgTagParser {
    fun getContents(urlToParse: String): LinkSourceContent? {
        return execute(urlToParse)
    }

    private fun execute(urlToParse: String): LinkSourceContent? {
        var linkSourceContent: LinkSourceContent?
        runBlocking {
            linkSourceContent = doInBackground(urlToParse)
        }
        return linkSourceContent
    }

    /**
     * Using withContext as we don't need parallel execution.
     * withContext return the result of single task.
     */
    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun doInBackground(urlToParse: String): LinkSourceContent? =
        withContext(Dispatchers.IO) {
            try {
                val response = Jsoup.connect(urlToParse)
                    .ignoreContentType(true)
                    .userAgent("Mozilla")
                    .referrer("http://www.google.com")
                    .timeout(12000)
                    .followRedirects(true)
                    .execute()
                val doc = response.parse()
                return@withContext organizeFetchedData(doc)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            null
        }

    private fun organizeFetchedData(doc: Document): LinkSourceContent {
        val linkSourceContent = LinkSourceContent("","","","","","")
        val ogTags = doc.select("meta[property^=og:]")
        when {
            ogTags.size > 0 ->
                ogTags.forEachIndexed { index, _ ->
                    val tag = ogTags[index]
                    val property = tag.attr("property")
                    val content = (tag.attr("content"))
                    when (property) {
                        OG_IMAGE -> {
                            linkSourceContent.image = content
                        }
                        OG_DESCRIPTION -> {
                            linkSourceContent.ogDescription = content
                        }
                        OG_URL -> {
                            linkSourceContent.ogUrl = content
                        }
                        OG_TITLE -> {
                            linkSourceContent.ogTitle = content
                        }
                        OG_SITE_NAME -> {
                            linkSourceContent.ogSiteName = content
                        }
                        OG_TYPE -> {
                            linkSourceContent.ogType = content
                        }
                    }
                }
        }
        return linkSourceContent
    }
}