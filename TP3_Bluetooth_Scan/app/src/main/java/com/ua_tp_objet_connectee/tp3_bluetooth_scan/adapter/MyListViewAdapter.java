package com.ua_tp_objet_connectee.tp3_bluetooth_scan.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ua_tp_objet_connectee.tp3_bluetooth_scan.R;
import com.ua_tp_objet_connectee.tp3_bluetooth_scan.model.MyListViewItem;

import java.util.ArrayList;
import java.util.List;

public class MyListViewAdapter extends ArrayAdapter<MyListViewItem> {

    private final List<MyListViewItem> myListViewItemList;    //  TIP : TO AVOID HAVING DUPLICATE IN OUR BLUETOOTH LIST
    private final LayoutInflater inflater;
    private final Context context;

    public MyListViewAdapter(Context context) {
        super(context, R.layout.list_view_item);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.myListViewItemList = new ArrayList<>();
    }

    //  TIP : TO AVOID HAVING DUPLICATE IN OUR BLUETOOTH LIST
    public boolean isStillOnMyItemList(MyListViewItem item) {
        for(MyListViewItem myItem : myListViewItemList) {
            if (myItem.getName().equals(item.getName()) && myItem.getAddress().equals(item.getAddress())) {
                return true;
            }
        }
        return false;
    }

    //  TIP : TO AVOID HAVING DUPLICATE IN OUR BLUETOOTH LIST
    public void addItem(MyListViewItem item) {
        if (! this.isStillOnMyItemList(item)) {
            this.myListViewItemList.add(item);
            this.add(item);
        }
    }

    public Context getContext() {
        return context;
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

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.list_view_item, null);
        MyListViewItem item = this.getItem(i);

        TextView bluetoothItemName = view.findViewById(R.id.bluetoothItemName);
        bluetoothItemName.setText(item.getName());
        TextView bluetoothItemAddress = view.findViewById(R.id.bluetoothItemAddress);
        bluetoothItemAddress.setText(item.getAddress());

        return view;
    }
}
