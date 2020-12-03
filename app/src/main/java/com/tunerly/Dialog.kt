package com.tunerly

import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import com.example.tuner.R

class Dialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        val title: String = getString(R.string.popup_title)
        val text: String = getString(R.string.popup)
        val ok: String = getString(R.string.ok)
        builder.setTitle(title)
        builder.setMessage(getSpannedText(text))
        builder.setPositiveButton(ok) { _, _ ->
        }
        return builder.create()
    }

    private fun getSpannedText(text: String): Spanned? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        } else {
            HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
        }
    }
}