package com.ua_tp_objet_connectee.tp1_bluetooth_scan.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ua_tp_objet_connectee.tp1_bluetooth_scan.R;
import com.ua_tp_objet_connectee.tp1_bluetooth_scan.model.MyListViewItem;

import java.util.ArrayList;
import java.util.List;

public class MyListViewAdapter extends ArrayAdapter<MyListViewItem> {
    private List<MyListViewItem> myListViewItemList;    //  ASTUCE : POUR EVITER D'AVOIR DES DOUBLONS DANS NOTRE LISTE DE BLUETOOTH
    private LayoutInflater inflater;
    private Context context;

    public MyListViewAdapter(Context context) {
        super(context, R.layout.list_view_item);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.myListViewItemList = new ArrayList<>();
    }

    //  ASTUCE : POUR EVITER D'AVOIR DES DOUBLONS DANS NOTRE LISTE DE BLUETOOTH
    public boolean isStillOnMyItemList(MyListViewItem item) {
        for(MyListViewItem myItem : myListViewItemList) {
            if (myItem.getName().equals(item.getName()) && myItem.getAddress().equals(item.getAddress())) {
                return true;
            }
        }
        return false;
    }

    public void addItem(MyListViewItem item) {
        //  ASTUCE : POUR EVITER D'AVOIR DES DOUBLONS DANS NOTRE LISTE DE BLUETOOTH
        if (! this.isStillOnMyItemList(item)) {
            this.myListViewItemList.add(item);
            this.add(item);
        }
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<MyListViewItem> getMyListViewItemList() {
        return myListViewItemList;
    }

    public void setMyListViewItemList(List<MyListViewItem> myListViewItemList) {
        this.myListViewItemList = myListViewItemList;
    }

    @Override
    public int getCount() {
        return this.myListViewItemList.size();
    }

    @Override
    public MyListViewItem getItem(int position) {
        return myListViewItemList.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.list_view_item, null);
        MyListViewItem item = this.getItem(i);

        TextView bluetoothItemName = view.findViewById(R.id.bluetoothItemName);
        bluetoothItemName.setText(item.getName());
        TextView bluetoothItemAdress = view.findViewById(R.id.bluetoothItemAdress);
        bluetoothItemAdress.setText(item.getAddress());

        return view;
    }
}
