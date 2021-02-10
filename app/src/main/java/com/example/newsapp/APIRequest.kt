package com.example.newsapp

import com.example.newsapp.api.NewsApiJSON
import retrofit2.http.GET

interface APIRequest {

    //get method using retrofit2
    @GET("/v1/latest-news?language=en&apiKey=gATYOlZGxcSIIXQiryJp1ZRgq6147Wvq3IIDbF2irUfAkpUn")
    suspend fun getNews():NewsApiJSON
}