package com.jattilainen.memebroker.TopPeople;

public class TopPeopleItem {
    private String name;
    private int money;
    private int order;

    public TopPeopleItem(String name, int money, int order) {
        this.name = name;
        this.money = money;
        this.order = order;
    }

    public String getName() {
        return name;
    }

    public int getMoney() {
        return money;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
