package com.jattilainen.memebroker.Assets;

public class AssetsItem {
    private Long totalExpenses;
    private String hash;
    private String language;
    private Long amount;
    private double ratio;
    private String url;
    public AssetsItem(String hash, String language, Long totalExpenses, Long amount, double ratio, String url) {
        this.amount = amount;
        this.totalExpenses = totalExpenses;
        this.hash = hash;
        this.language = language;
        this.ratio = ratio;
        this.url = url;
    }

    public AssetsItem() {
    }

    public Long getTotalExpenses() {
        return totalExpenses;
    }

    public String getHash() {
        return hash;
    }

    public double getRatio() {
        return ratio;
    }

    public String getLanguage() {
        return language;
    }

    public void setTotalExpenses(Long totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Long getAmount() {
        return amount;
    }


    public void setLanguage(String language) {
        this.language = language;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getUrl() {
        return url;
    }
}
