package com.hushbunny.app.uitls

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.browse(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}