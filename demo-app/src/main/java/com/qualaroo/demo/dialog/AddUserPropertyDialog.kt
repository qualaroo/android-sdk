package com.qualaroo.demo.dialog

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast

import com.qualaroo.Qualaroo

import qualaroo.com.QualarooMobileDemo.R

class AddUserPropertyDialog : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.qualaroo__demo_dialog_set_user_property, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userPropertyName = view.findViewById<EditText>(R.id.qualaroo__demo_dialog_property_name)
        val userPropertyValue = view.findViewById<EditText>(R.id.qualaroo__demo_dialog_property_value)

        view.findViewById<View>(R.id.qualaroo__demo_dialog_property_confirm).setOnClickListener {
            val name = userPropertyName.text.toString()
            val value = userPropertyValue.text.toString()
            try {
                Qualaroo.getInstance().setUserProperty(name, value)
            } catch (e: Exception) {
                Toast.makeText(context, "Crash occured. Did you set a proper API key?", Toast.LENGTH_SHORT).show()
                Log.e("Demo", e.message, e)
            }

            dismissAllowingStateLoss()
        }
        view.findViewById<View>(R.id.qualaroo__demo_dialog_property_cancel).setOnClickListener { dismissAllowingStateLoss() }
    }
}
