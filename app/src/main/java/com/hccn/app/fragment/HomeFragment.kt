package com.hccn.app.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hccn.app.R
import com.hccn.app.data.Api
import com.hccn.app.data.HitokotoResponse
import com.hccn.app.net.AppRetrofit.getAppRetrofit
import com.hccn.app.utils.LogUtil
import com.hccn.sisyphus.Sisyphus
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class HomeFragment : Fragment(R.layout.home_fragment) {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tv_flavor).text = Sisyphus.getFlavorEnvironment(context)
        view.findViewById<TextView>(R.id.tv_host).text = Sisyphus.getUrlEnvironment(context)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener { getHitokoto() }
    }

    override fun onResume() {
        super.onResume()
        getHitokoto()
    }

    private fun getHitokoto() {
        context?.let {
            getAppRetrofit(it)
                .create(Api::class.java)
                .getHitokoto()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<HitokotoResponse> {
                    override fun onSubscribe(d: Disposable) {
                        LogUtil.i(it, TAG, "onSubscribe")
                        swipeRefreshLayout.isRefreshing = true
                    }

                    @SuppressLint("SetTextI18n")
                    override fun onNext(value: HitokotoResponse) {
                        LogUtil.e(it, TAG, "onNext: $value")
                        view?.findViewById<TextView>(R.id.tv_text)?.text = value.hitokoto
                        view?.findViewById<TextView>(R.id.tv_form)?.text = "—— 「" + value.from + "」"
                    }

                    override fun onError(e: Throwable) {
                        LogUtil.e(it, TAG, "onError: ", e)
                        swipeRefreshLayout.isRefreshing = false
                    }

                    override fun onComplete() {
                        LogUtil.i(it, TAG, "onSubscribe")
                        swipeRefreshLayout.isRefreshing = false
                    }
                })
        }
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}