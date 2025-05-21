package com.example.activitymanager

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.Response

data class ZenQuote(
    val q: String,   // quote
    val a: String    // author
)

interface ZenQuoteApi {
    @GET("api/random")
    suspend fun getRandomQuote(): Response<List<ZenQuote>>
}

class ZenQuoteService {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://zenquotes.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(ZenQuoteApi::class.java)

    suspend fun fetchQuoteText(): String? {
        return try {
            val response = api.getRandomQuote()
            if (response.isSuccessful) {
                response.body()?.firstOrNull()?.q
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
