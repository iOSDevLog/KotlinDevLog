package com.iosdevlog.actionbarcompat_shareactionprovider.entity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import com.iosdevlog.actionbarcompat_shareactionprovider.provider.AssetContentProvider

/**
 * Created by iosdevlog on 2017/12/6.
 */
class ContentItem {

    val contentType: Int
    val contentResourceId: Int
    private val contentAssetFilePath: String?

    /**
     * @return Uri to the content
     */
    // If this content has an asset, then return a AssetProvider Uri
    val contentUri: Uri?
        get() = if (!TextUtils.isEmpty(contentAssetFilePath)) {
            Uri.parse("content://" + AssetContentProvider.CONTENT_URI + "/" + contentAssetFilePath)
        } else {
            null
        }

    /**
     * Creates a ContentItem with the specified type, referencing a resource id.
     *
     * @param type - One of [.CONTENT_TYPE_IMAGE] or [.CONTENT_TYPE_TEXT]
     * @param resourceId - Resource ID to use for this item's content
     */
    constructor(type: Int, resourceId: Int) {
        contentType = type
        contentResourceId = resourceId
        contentAssetFilePath = null
    }

    /**
     * Creates a ContentItem with the specified type, referencing an asset file path.
     *
     * @param type - One of [.CONTENT_TYPE_IMAGE] or [.CONTENT_TYPE_TEXT]
     * @param assetFilePath - File path from the application's asset for this item's content
     */
    constructor(type: Int, assetFilePath: String) {
        contentType = type
        contentAssetFilePath = assetFilePath
        contentResourceId = 0
    }

    /**
     * Returns an [android.content.Intent] which can be used to share this item's content with other
     * applications.
     *
     * @param context - Context to be used for fetching resources if needed
     * @return Intent to be given to a ShareActionProvider.
     */
    fun getShareIntent(context: Context): Intent {
        val intent = Intent(Intent.ACTION_SEND)

        when (contentType) {
            CONTENT_TYPE_IMAGE -> {
                intent.type = "image/jpg"
                // Bundle the asset content uri as the EXTRA_STREAM uri
                intent.putExtra(Intent.EXTRA_STREAM, contentUri)
            }

            CONTENT_TYPE_TEXT -> {
                intent.type = "text/plain"
                // Get the string resource and bundle it as an intent extra
                intent.putExtra(Intent.EXTRA_TEXT, context.getString(contentResourceId))
            }
        }

        return intent
    }

    companion object {
        // Used to signify an image content type
        val CONTENT_TYPE_IMAGE = 0
        // Used to signify a text/string content type
        val CONTENT_TYPE_TEXT = 1
    }

}