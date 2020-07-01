package com.mycelium.bequant.remote.repositories

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mycelium.bequant.BequantPreference
import com.mycelium.bequant.Constants.ACCOUNT_ENDPOINT
import com.mycelium.bequant.remote.BequantApiService
import com.mycelium.bequant.remote.model.BequantBalance
import com.mycelium.bequant.remote.trading.model.Ticker
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory


class ApiRepository {

    fun balances(success: (List<BequantBalance>) -> Unit, error: (Int, String) -> Unit) {
        service.balance().enqueue(object : Callback<List<BequantBalance>> {
            override fun onFailure(call: Call<List<BequantBalance>>, t: Throwable) {
                error.invoke(0, t.message ?: "")
            }

            override fun onResponse(call: Call<List<BequantBalance>>, response: Response<List<BequantBalance>>) {
                if (response.isSuccessful) {
                    success.invoke(response.body() ?: listOf())
                } else {
                    error.invoke(response.code(), response.errorBody()?.string() ?: "")
                }
            }
        })
    }

    companion object {
        fun adopt(currency: String) = if (currency.startsWith("t")) currency.substring(1) else currency



        private val objectMapper = ObjectMapper()
                .registerKotlinModule()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)

        val repository by lazy { ApiRepository() }

        val retrofitBuilder by lazy { retrofitBuilder() }

        val service by lazy {
            retrofitBuilder
                    .build()
                    .create(BequantApiService::class.java)
        }

        private fun retrofitBuilder(): Retrofit.Builder {
            return Retrofit.Builder()
                    .baseUrl(ACCOUNT_ENDPOINT)
                    .client(OkHttpClient.Builder()
                            .addInterceptor {
                                it.proceed(it.request().newBuilder().apply {
                                    header("Content-Type", "application/json")
                                    header("Authorization",
                                            Credentials.basic(BequantPreference.getPublicKey(),
                                                    BequantPreference.getPrivateKey()))
                                }.build())
                            }
                            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                            .build())
                    .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        }
    }
}