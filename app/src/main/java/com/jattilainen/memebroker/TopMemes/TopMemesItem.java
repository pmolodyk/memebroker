package com.jattilainen.memebroker.TopMemes;

public class TopMemesItem {
    private String hash;
    private String language;
    private double ratio;
    private String url;
    public TopMemesItem(String hash, String language, double ratio, String url) {
        this.hash = hash;
        this.language = language;
        this.ratio = ratio;
        this.url = url;
    }

    public String getHash() {
        return hash;
    }

    public String getLanguage() {
        return language;
    }

    public double getRatio() {
        return ratio;
    }

    public String getUrl() {
        return url;
    }
}
