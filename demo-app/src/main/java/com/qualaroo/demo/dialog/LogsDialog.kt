/*
 * Copyright (c) 2018, Qualaroo, Inc. All Rights Reserved.
 *
 * Please refer to the LICENSE.md file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.qualaroo.demo.dialog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.qualaroo.demo.util.Logcat
import qualaroo.com.QualarooMobileDemo.R

class LogsDialog : BottomSheetDialogFragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.qualaroo__demo_dialog_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logsView = view.findViewById<TextView>(R.id.qualaroo__demo_dialog_logs)
        logsView.movementMethod = ScrollingMovementMethod()
        logsView.text = Logcat.getLogs()

        view.findViewById<View>(R.id.qualaroo__demo_dialog_logs_close).setOnClickListener { dismissAllowingStateLoss() }
        view.findViewById<View>(R.id.qualaroo__demo_dialog_logs_copy).setOnClickListener { copyContentToClipboard(logsView) }
    }

    private fun copyContentToClipboard(textView: TextView) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        if (clipboard != null) {
            val clip = ClipData.newPlainText("logcat", textView.text.toString())
            clipboard.primaryClip = clip
            Toast.makeText(context, "Logs copied to clipboard", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Could not copy logs", Toast.LENGTH_SHORT).show()
        }
    }
}
