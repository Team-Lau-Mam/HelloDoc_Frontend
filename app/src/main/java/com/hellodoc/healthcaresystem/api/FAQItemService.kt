package com.hellodoc.healthcaresystem.api

import com.hellodoc.healthcaresystem.responsemodel.GetFAQItemResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers

interface FAQItemService {
    @Headers("Content-Type: application/json")
    @GET("faqitem/get-all")
    suspend fun getFAQItems(): Response<List<GetFAQItemResponse>>
}