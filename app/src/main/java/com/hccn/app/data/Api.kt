package com.hccn.app.data

import io.reactivex.Observable
import retrofit2.http.GET

interface Api {
    @GET("/")
    fun getHitokoto(): Observable<HitokotoResponse>
}