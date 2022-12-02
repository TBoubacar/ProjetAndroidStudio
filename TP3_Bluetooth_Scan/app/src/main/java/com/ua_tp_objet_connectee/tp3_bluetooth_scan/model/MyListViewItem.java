package com.ua_tp_objet_connectee.tp3_bluetooth_scan.model;

public class MyListViewItem {

    private final String name;
    private final String address;

    public MyListViewItem(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

}
