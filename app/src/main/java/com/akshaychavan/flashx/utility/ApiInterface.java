package com.akshaychavan.flashx.utility;

/**
 * Created by Akshay Chavan on 12,February,2021
 * akshay.chavan@finiq.com
 * FinIQ Consulting India
 */


import com.akshaychavan.flashx.pojo.WordDataPojo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiInterface {

    //    https://api.dictionaryapi.dev/api/v2/entries/en_US/Voracious

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("{word}")
    Call<List<WordDataPojo>> getWordData(@Path("word") String word);

}
