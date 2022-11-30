package com.ua_tp_objet_connectee.tp2_bluetooth_scan.model;

public class MyListViewItem {
    private String name;
    private String address;

    public MyListViewItem(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
