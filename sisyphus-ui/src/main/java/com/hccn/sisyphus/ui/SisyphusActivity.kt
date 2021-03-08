package com.hccn.sisyphus.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.hccn.sisyphus.Constants
import com.hccn.sisyphus.R
import com.hccn.sisyphus.bean.EnvironmentBean
import com.hccn.sisyphus.bean.ModuleBean
import java.io.File
import java.io.FileFilter
import java.util.*
import kotlin.system.exitProcess


class SisyphusActivity : FragmentActivity(R.layout.sisyphus_activity) {

    private var itemAdapter: ItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<View>(R.id.bt_back).setOnClickListener { finish() }
        findViewById<View>(R.id.tv_clean_all_user_files).setOnClickListener {
            Utils().cleanAllUserFilesWithOutSisyphusSpFile()
            Toast.makeText(this, "清除用户数据成功", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.tv_relaunch_app).setOnClickListener { Utils().relaunchApp(true) }
        itemAdapter = ItemAdapter(initEnvironmentBeans())
        findViewById<ListView>(R.id.list_view).adapter = itemAdapter
    }

    private fun initEnvironmentBeans(): List<EnvironmentBean> {
        val environmentBeans = ArrayList<EnvironmentBean>()
        val sisyphusClass =
            Class.forName(Constants.PACKAGE_NAME + "." + Constants.SISYPHUS_FILE_NAME)
        val getEnvironmentConfigMethod =
            sisyphusClass.getMethod(Constants.METHOD_NAME_GET_MODULE_LIST)
        val modules = getEnvironmentConfigMethod.invoke(sisyphusClass.newInstance()) as ArrayList<*>
        for (module in modules) {
            if (module is ModuleBean) {
                val environmentModule = EnvironmentBean("", "", module.alias, module, false)
                environmentBeans.add(environmentModule)
                environmentBeans.addAll(module.environments)
            }
        }
        var currentModuleName = ""
        var xxModuleCurrentEnvironment: EnvironmentBean? = null
        for (environmentBean in environmentBeans) {
            if (!TextUtils.equals(
                    environmentBean.module.name,
                    currentModuleName
                ) || xxModuleCurrentEnvironment == null
            ) {
                currentModuleName = environmentBean.module.name
                val getXXEnvironmentBeanMethod = sisyphusClass.getMethod(
                    "get" + environmentBean.module.name + "EnvironmentBean",
                    Context::class.java
                )
                xxModuleCurrentEnvironment = getXXEnvironmentBeanMethod.invoke(
                    sisyphusClass.newInstance(),
                    this
                ) as EnvironmentBean
            }
            environmentBean.isChecked = environmentBean == xxModuleCurrentEnvironment
        }
        return environmentBeans
    }

    internal inner class ItemAdapter(private val environmentBeans: List<EnvironmentBean>) :
        BaseAdapter() {

        override fun getCount(): Int {
            return environmentBeans.size
        }

        override fun getItem(position: Int): EnvironmentBean {
            return environmentBeans[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val environmentBean = getItem(position)
            return when {
                getItemViewType(position) == ItemViewType.TYPE_MODULE.ordinal -> {
                    getModuleView(parent, environmentBean)
                }
                getItemViewType(position) == ItemViewType.TYPE_ENVIRONMENT.ordinal -> {
                    getEnvironmentView(parent, environmentBean)
                }
                else -> throw EnumConstantNotPresentException(ItemViewType::class.java, "")
            }
        }

        private fun getModuleView(parent: ViewGroup, environmentBean: EnvironmentBean): View {
            val moduleView = LayoutInflater.from(parent.context)
                .inflate(R.layout.sisyphus_item_module, parent, false)
            val moduleName = environmentBean.module.name
            val alias = environmentBean.alias
            moduleView.findViewById<TextView>(R.id.tv_name).text =
                if (TextUtils.isEmpty(alias)) moduleName else alias
            return moduleView
        }

        private fun getEnvironmentView(parent: ViewGroup, environmentBean: EnvironmentBean): View {
            val environmentView = LayoutInflater.from(parent.context)
                .inflate(R.layout.sisyphus_item_environment, parent, false)
            val alias = environmentBean.alias
            environmentView.findViewById<TextView>(R.id.tv_name).text =
                if (TextUtils.isEmpty(alias)) environmentBean.name else alias
            environmentView.findViewById<TextView>(R.id.tv_value).text = environmentBean.value
            environmentView.findViewById<ImageView>(R.id.iv_mark).visibility =
                if (environmentBean.isChecked) View.VISIBLE else View.INVISIBLE
            environmentView.setOnClickListener { setEnvironment(environmentBean) }
            return environmentView
        }

        private fun setEnvironment(environmentBean: EnvironmentBean) {
            val sisyphusClass =
                Class.forName(Constants.PACKAGE_NAME + "." + Constants.SISYPHUS_FILE_NAME)
            val method = sisyphusClass.getMethod(
                "set" + environmentBean.module.name + "Environment",
                Context::class.java,
                EnvironmentBean::class.java
            )
            method.invoke(sisyphusClass.newInstance(), this@SisyphusActivity, environmentBean)
            for (bean in environmentBeans) {
                if (bean.module == environmentBean.module) {
                    bean.isChecked = bean == environmentBean
                }
            }
            itemAdapter?.notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return if (TextUtils.isEmpty(getItem(position).name)) {
                ItemViewType.TYPE_MODULE
            } else {
                ItemViewType.TYPE_ENVIRONMENT
            }.ordinal
        }
    }

    internal inner class Utils {
        /**
         * Clean all user files without sisyphus.
         */
        fun cleanAllUserFilesWithOutSisyphusSpFile() {
            cleanInternalCache()
            cleanInternalFiles()
            cleanInternalDbs()
            cleanInternalSpWithOutSisyphusSpFile()
            cleanExternalCache()
        }

        /**
         * Relaunch the application.
         *
         * @param isKillProcess True to kill the process, false otherwise.
         */
        fun relaunchApp(isKillProcess: Boolean) {
            val intent: Intent = getLaunchAppIntent(packageName) ?: return
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            if (!isKillProcess) return
            Process.killProcess(Process.myPid())
            exitProcess(0)
        }

        /**
         * Return the intent of launch app.
         *
         * @param pkgName The name of the package.
         * @return the intent of launch app
         */
        private fun getLaunchAppIntent(pkgName: String): Intent? {
            val launcherActivity: String = getLauncherActivity(pkgName)
            if (isSpace(launcherActivity)) return null
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.setClassName(pkgName, launcherActivity)
            return intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        /**
         * Return the name of launcher activity.
         *
         * @param pkg The name of the package.
         * @return the name of launcher activity
         */
        @SuppressLint("QueryPermissionsNeeded")
        private fun getLauncherActivity(pkg: String): String {
            if (isSpace(pkg)) return ""
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.setPackage(pkg)
            val info = packageManager.queryIntentActivities(intent, 0)
            return if (info.size == 0) "" else info[0].activityInfo.name
        }

        /**
         * Return whether the string is null or white space.
         *
         * @param s The string.
         * @return `true`: yes<br></br> `false`: no
         */
        private fun isSpace(s: String?): Boolean {
            if (s == null) return true
            var i = 0
            val len = s.length
            while (i < len) {
                if (!Character.isWhitespace(s[i])) {
                    return false
                }
                ++i
            }
            return true
        }

        /**
         * Clean the internal cache.
         *
         * directory: /data/data/package/cache
         *
         * @return `true`: success<br></br>`false`: fail
         */
        private fun cleanInternalCache(): Boolean {
            return deleteAllInDir(cacheDir)
        }

        /**
         * Clean the internal files.
         *
         * directory: /data/data/package/files
         *
         * @return `true`: success<br></br>`false`: fail
         */
        private fun cleanInternalFiles(): Boolean {
            return deleteAllInDir(filesDir)
        }

        /**
         * Clean the internal databases.
         *
         * directory: /data/data/package/databases
         *
         * @return `true`: success<br></br>`false`: fail
         */
        private fun cleanInternalDbs(): Boolean {
            return deleteAllInDir(File(filesDir.parent, "databases"))
        }

        /**
         * Clean the internal shared preferences.
         * Without Sisyphus
         *
         * directory: /data/data/package/shared_prefs
         *
         * @return `true`: success<br></br>`false`: fail
         */
        private fun cleanInternalSpWithOutSisyphusSpFile(): Boolean {
            return deleteFilesInDirWithFilter(File(filesDir.parent, "shared_prefs")) { file ->
                !file.name.contains(
                    packageName + "." + Constants.SISYPHUS_FILE_NAME.toLowerCase(
                        Locale.getDefault()
                    )
                )
            }
        }

        /**
         * Clean the external cache.
         *
         * directory: /storage/emulated/0/android/data/package/cache
         *
         * @return `true`: success<br></br>`false`: fail
         */
        private fun cleanExternalCache(): Boolean {
            return (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
                    && deleteAllInDir(externalCacheDir))
        }

        /**
         * Delete the all in directory.
         *
         * @param dir The directory.
         * @return `true`: success<br></br>`false`: fail
         */
        private fun deleteAllInDir(dir: File?): Boolean {
            return deleteFilesInDirWithFilter(dir) { file -> !file.name.contains("Sisyphus") }
        }

        /**
         * Delete all files that satisfy the filter in directory.
         *
         * @param dir    The directory.
         * @param filter The filter.
         * @return `true`: success<br></br>`false`: fail
         */
        private fun deleteFilesInDirWithFilter(dir: File?, filter: FileFilter?): Boolean {
            if (dir == null || filter == null) return false
            // dir doesn't exist then return true
            if (!dir.exists()) return true
            // dir isn't a directory then return false
            if (!dir.isDirectory) return false
            val files = dir.listFiles()
            if (files != null && files.isNotEmpty()) {
                for (file in files) {
                    if (filter.accept(file)) {
                        if (file.isFile) {
                            if (!file.delete()) return false
                        } else if (file.isDirectory) {
                            if (!deleteDir(file)) return false
                        }
                    }
                }
            }
            return true
        }

        /**
         * Delete the directory.
         *
         * @param dir The directory.
         * @return `true`: success<br></br>`false`: fail
         */
        private fun deleteDir(dir: File?): Boolean {
            if (dir == null) return false
            // dir doesn't exist then return true
            if (!dir.exists()) return true
            // dir isn't a directory then return false
            if (!dir.isDirectory) return false
            val files = dir.listFiles()
            if (files != null && files.isNotEmpty()) {
                for (file in files) {
                    if (file.isFile) {
                        if (!file.delete()) return false
                    } else if (file.isDirectory) {
                        if (!deleteDir(file)) return false
                    }
                }
            }
            return dir.delete()
        }
    }

    companion object {
        enum class ItemViewType {
            TYPE_MODULE, TYPE_ENVIRONMENT
        }

        @JvmStatic
        fun launch(context: Context) {
            context.startActivity(Intent(context, SisyphusActivity::class.java))
        }
    }
}