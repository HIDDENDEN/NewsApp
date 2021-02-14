package com.example.newsapp

import com.example.newsapp.api.NewsApiJSON
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

interface APIRequest {

    //get method using retrofit2
    @GET
    fun getNews(@Url url:String ):Observable<NewsApiJSON>

    @GET
    fun getKeyWordNews(@Url url:String):Observable<NewsApiJSON>

    @GET
    fun getRegionNews(@Url url: String):Observable<NewsApiJSON>
}