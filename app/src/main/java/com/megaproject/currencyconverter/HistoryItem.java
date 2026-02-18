package com.megaproject.currencyconverter;

public class HistoryItem {
    public String fromCode;
    public String toCode;
    public double fromAmount;
    public double toAmount;
    public String date;

    public HistoryItem(String fromCode, String toCode, double fromAmount, double toAmount, String date) {
        this.fromCode = fromCode;
        this.toCode = toCode;
        this.fromAmount = fromAmount;
        this.toAmount = toAmount;
        this.date = date;
    }
}
