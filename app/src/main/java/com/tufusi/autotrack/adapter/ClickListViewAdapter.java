package com.tufusi.autotrack.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tufusi.autotrack.R;

import java.util.Locale;

/**
 * Created by LeoCheung on 2020/11/24.
 *
 * @description
 */
public class ClickListViewAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater;

    public ClickListViewAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 8;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_listview, parent, false);
            ViewHolder holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.title.setText(String.format(Locale.CHINA, "位置 %d", position));

        return convertView;
    }

    private static class ViewHolder {
        ViewHolder(View viewRoot) {
            title = viewRoot.findViewById(R.id.title);
        }

        public TextView title;
    }

} 