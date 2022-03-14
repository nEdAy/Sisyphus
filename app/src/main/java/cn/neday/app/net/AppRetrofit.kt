package cn.neday.app.net

import android.content.Context
import cn.neday.sisyphus.Sisyphus
import com.readystatesoftware.chuck.ChuckInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object AppRetrofit {
    private lateinit var sAppHost: String
    private lateinit var sAppRetrofit: Retrofit

    fun getAppRetrofit(context: Context): Retrofit {
        val host = Sisyphus.getUrlEnvironment(context)
        if (!this::sAppRetrofit.isInitialized || !this::sAppHost.isInitialized || sAppHost != host) {
            sAppHost = host
            sAppRetrofit = Retrofit.Builder()
                .baseUrl(sAppHost)
                .client(getAppOkHttpClient(context))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return sAppRetrofit
    }

    private fun getAppOkHttpClient(context: Context): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
        if (Sisyphus.getNetworkInspectorEnvironmentBean(context) == Sisyphus.NETWORKINSPECTOR_OPEN_ENVIRONMENT) {
            okHttpClient.addInterceptor(ChuckInterceptor(context))
        }
        return okHttpClient.build()
    }
}