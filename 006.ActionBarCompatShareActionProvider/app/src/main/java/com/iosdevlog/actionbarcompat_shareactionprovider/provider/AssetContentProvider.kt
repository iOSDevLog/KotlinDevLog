package com.iosdevlog.actionbarcompat_shareactionprovider.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import java.io.FileNotFoundException
import java.io.IOException

class AssetContentProvider : ContentProvider() {

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        // Do not support delete requests.
        return 0
    }

    override fun getType(uri: Uri): String? {
        // Do not support returning the data type
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // Do not support insert requests.
        return null
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        // Do not support query requests.
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        // Do not support update requests.
        return 0
    }

    override fun openAssetFile(uri: Uri?, mode: String?): AssetFileDescriptor {
        val assetName = uri?.lastPathSegment

        if (TextUtils.isEmpty(assetName)) {
            throw FileNotFoundException()
        }

        return try {
            val am = context!!.assets
            am.openFd(assetName)
        } catch (e: IOException) {
            e.printStackTrace()
            super.openAssetFile(uri, mode)
        }
    }

    companion object {
        val CONTENT_URI = "com.iosdevlog.actionbarcompatshareactionprovider"
    }
}
