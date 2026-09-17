package com.jini.indicator.init

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

class IndicatorContentProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        IndicatorSDK.initialize(context ?: return false)
        return true
    }
    override fun query(u: Uri, p: Array<String>?, s: String?, a: Array<String>?, o: String?): Cursor? = null
    override fun getType(u: Uri): String? = null
    override fun insert(u: Uri, v: ContentValues?): Uri? = null
    override fun delete(u: Uri, s: String?, a: Array<String>?): Int = 0
    override fun update(u: Uri, v: ContentValues?, s: String?, a: Array<String>?): Int = 0
}
