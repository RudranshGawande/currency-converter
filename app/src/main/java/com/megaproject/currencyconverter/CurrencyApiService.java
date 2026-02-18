package com.megaproject.currencyconverter;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CurrencyApiService {
    @GET("latest/{base}")
    Call<ExchangeRateResponse> getLatestRates(@Path("base") String base);
}
