package com.jattilainen.memebroker.ShortList;

/**
 * Created by Vadim on 31.05.2018.
 */

public class ShortlistItem {
    private long startPrice;
    private String hash;
    private String language;
    private double ratio;
    private String url;
    public ShortlistItem(long startPrice, String hash, String language, double ratio, String url) {
        this.startPrice = startPrice;
        this.hash = hash;
        this.language = language;
        this.ratio = ratio;
        this.url = url;
    }

    public ShortlistItem() {
    }

    public long getStartPrice() {
        return startPrice;
    }

    public String getHash() {
        return hash;
    }

    public String getLanguage() {
        return language;
    }

    public void setStartPrice(long startPrice) {
        this.startPrice = startPrice;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public double getRatio() {
        return ratio;
    }

    public String getUrl() {
        return url;
    }
}
