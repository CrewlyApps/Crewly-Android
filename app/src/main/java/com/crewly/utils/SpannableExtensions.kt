package com.crewly.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Spannable
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View

/**
 * Created by Derek on 16/08/2018
 */

/**
 * Adds a click span that opens up to a url
 * @param url The url to open when clicked
 * @param startIndex The starting index of the text to span
 * @param endIndex The end index of the text to span
 */
fun Spannable.addUrlClickSpan(context: Context, url: String, startIndex: Int, endIndex: Int): Spannable {

    this.setSpan(object: ClickableSpan() {
        override fun onClick(view: View) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }

    }, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

    return this
}