package com.megaproject.currencyconverter;

public class CurrencyItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private int type;
    private String headerTitle;

    private String code;
    private String name;
    private String flagUrl;

    // Constructor for Header
    public CurrencyItem(String headerTitle) {
        this.type = TYPE_HEADER;
        this.headerTitle = headerTitle;
    }

    // Constructor for Item
    public CurrencyItem(String code, String name, String flagUrl) {
        this.type = TYPE_ITEM;
        this.code = code;
        this.name = name;
        this.flagUrl = flagUrl;
    }

    public int getType() { return type; }
    public String getHeaderTitle() { return headerTitle; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getFlagUrl() { return flagUrl; }
}
