package com.tufusi.autotrack.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.tufusi.autotrack.R;
import com.tufusi.autotrack.ui.ClickExpandableListViewTestActivity;

import java.util.List;
import java.util.Map;

/**
 * Created by LeoCheung on 2020/11/24.
 *
 * @description
 */
public class ClickExpandableListViewAdapter extends BaseExpandableListAdapter {

    private LayoutInflater mLayoutInflater;
    private Context context;
    private Map<String, List<String>> mDataSet;
    private String[] parentList;

    public ClickExpandableListViewAdapter(Context context, Map<String, List<String>> dataSet, String[] parentList) {
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.mDataSet = dataSet;
        this.parentList = parentList;
    }

    //  获得某个父项的某个子项
    @Override
    public Object getChild(int parentPos, int childPos) {
        return mDataSet.get(parentList[parentPos]).get(childPos);
    }

    //  获得父项的数量
    @Override
    public int getGroupCount() {
        if (mDataSet == null) {
            return 0;
        }
        return mDataSet.size();
    }

    //  获得某个父项的子项数目
    @Override
    public int getChildrenCount(int parentPos) {
        if (mDataSet.get(parentList[parentPos]) == null) {
            return 0;
        }
        return mDataSet.get(parentList[parentPos]).size();
    }

    //  获得某个父项
    @Override
    public Object getGroup(int parentPos) {
        return mDataSet.get(parentList[parentPos]);
    }

    //  获得某个父项的id
    @Override
    public long getGroupId(int parentPos) {
        return parentPos;
    }

    //  获得某个父项的某个子项的id
    @Override
    public long getChildId(int parentPos, int childPos) {
        return childPos;
    }

    //  按函数的名字来理解应该是是否具有稳定的id，这个函数目前一直都是返回false，没有去改动过
    @Override
    public boolean hasStableIds() {
        return false;
    }

    //  获得父项显示的view
    @Override
    public View getGroupView(int parentPos, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.parent_item, viewGroup, false);
        }
        view.setTag(R.layout.parent_item, parentPos);
        view.setTag(R.layout.child_item, -1);
        TextView text = view.findViewById(R.id.parent_title);
        text.setText(parentList[parentPos]);
        return view;
    }

    //  获得子项显示的view
    @SuppressWarnings("all")
    @Override
    public View getChildView(int parentPos, int childPos, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.child_item, null);
        }
        view.setTag(R.layout.parent_item, parentPos);
        view.setTag(R.layout.child_item, childPos);
        TextView text = view.findViewById(R.id.child_title);
        text.setText(mDataSet.get(parentList[parentPos]).get(childPos));
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "点到了内置的TextView", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    // 子项是否可选中，如果需要设置子项的点击事件，需要返回true
    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
} 