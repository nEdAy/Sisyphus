package cn.neday.app

import android.os.Bundle
import android.util.Log
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.neday.app.fragment.HomeFragment
import cn.neday.app.fragment.SettingsFragment
import cn.neday.sisyphus.Sisyphus
import cn.neday.sisyphus.bean.EnvironmentBean
import cn.neday.sisyphus.bean.ModuleBean
import cn.neday.sisyphus.listener.OnEnvironmentChangeListener

class MainActivity : AppCompatActivity(), OnEnvironmentChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        title = getString(R.string.app_name)
        val homeFragment = HomeFragment()
        val settingsFragment = SettingsFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, homeFragment, HomeFragment::class.java.simpleName)
        transaction.commit()
        val radioGroup = findViewById<RadioGroup>(R.id.radio_group)
        radioGroup.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            if (checkedId == R.id.radio_home) {
                fragmentTransaction.replace(
                    R.id.frame_layout,
                    homeFragment,
                    HomeFragment::class.java.simpleName
                )
            } else if (checkedId == R.id.radio_settings) {
                fragmentTransaction.replace(
                    R.id.frame_layout,
                    settingsFragment,
                    SettingsFragment::class.java.simpleName
                )
            }
            fragmentTransaction.commit()
        }
        Sisyphus.addOnEnvironmentChangeListener(this)
    }

    override fun onEnvironmentChanged(
        module: ModuleBean,
        oldEnvironment: EnvironmentBean,
        newEnvironment: EnvironmentBean
    ) {
        Log.e(
            TAG, "Module=${module.name},OldEnvironment=${oldEnvironment.name}," +
                    "oldUrl=${oldEnvironment.value},newEnvironment=${newEnvironment.name},newUrl=${newEnvironment.value}"
        )
        Toast.makeText(
            this,
            "已将${module.name}从${oldEnvironment.name}：${oldEnvironment.value}切换到${newEnvironment.name}：${newEnvironment.value}",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Sisyphus.removeOnEnvironmentChangeListener(this)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}