package cn.neday.app.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import cn.neday.app.BuildConfig
import cn.neday.app.R
import cn.neday.sisyphus.ui.SisyphusActivity

class SettingsFragment : Fragment(R.layout.settings_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!BuildConfig.DEBUG && BuildConfig.FLAVOR === "prod") {
            view.findViewById<View>(R.id.bt_switch_environment).visibility = View.GONE
        } else {
            view.findViewById<View>(R.id.bt_switch_environment)
                .setOnClickListener { context?.let { SisyphusActivity.launch(it) } }
        }
    }
}