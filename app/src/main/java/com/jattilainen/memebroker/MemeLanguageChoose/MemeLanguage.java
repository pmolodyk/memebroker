package com.jattilainen.memebroker.MemeLanguageChoose;

class MemeLanguage {
    private String translatedLang;
    private String originLang;

    public MemeLanguage(String translatedLang, String originLang) {
        this.translatedLang = translatedLang;
        this.originLang = originLang;
    }

    public String getTranslatedLang() {
        return translatedLang;
    }

    public String getOriginLang() {
        return originLang;
    }
}
