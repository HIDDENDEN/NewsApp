package com.example.newsapp

import com.example.newsapp.api.NewsApiJSON
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.http.GET

interface APIRequest {

    //get method using retrofit2
    @GET("/v1/latest-news?language=en&apiKey=gATYOlZGxcSIIXQiryJp1ZRgq6147Wvq3IIDbF2irUfAkpUn")
//    /*suspend*/ suspend and Observable. Retrofit puts priority on the suspend and thinks the return type should be Observable, but Observable is not a serializable data container.
    fun getNews():Observable<NewsApiJSON>

}