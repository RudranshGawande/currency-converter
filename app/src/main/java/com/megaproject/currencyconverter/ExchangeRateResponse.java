package com.megaproject.currencyconverter;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ExchangeRateResponse {
    @SerializedName("base")
    public String base;
    
    @SerializedName("date")
    public String date;
    
    @SerializedName("rates")
    public Map<String, Double> rates;
}
