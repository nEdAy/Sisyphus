package com.hccn.app.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hccn.app.BuildConfig
import com.hccn.app.R
import com.hccn.sisyphus.ui.SisyphusActivity

class SettingsFragment : Fragment(R.layout.settings_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (BuildConfig.FLAVOR === "prod") {
            view.findViewById<View>(R.id.bt_switch_environment).visibility = View.GONE
        } else {
            view.findViewById<View>(R.id.bt_switch_environment)
                .setOnClickListener { context?.let { SisyphusActivity.launch(it) } }
        }
    }
}