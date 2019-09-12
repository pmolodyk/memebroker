package com.jattilainen.memebroker.NewMemes;

public class NewMemesItem {
    private String hash;
    private String language;
    private String url;
    private double ratio;

    public NewMemesItem(String hash, String language, double ratio, String url) {
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
