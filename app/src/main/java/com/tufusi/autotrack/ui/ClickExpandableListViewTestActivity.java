package com.tufusi.autotrack.ui;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.tufusi.autotrack.R;
import com.tufusi.autotrack.adapter.ClickExpandableListViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickExpandableListViewTestActivity extends AppCompatActivity {

    private Map<String, List<String>> mDataSet = new HashMap<>();
    private String[] parentList = new String[]{"first", "second", "third"};
    private List<String> childrenList1 = new ArrayList<>();
    private List<String> childrenList2 = new ArrayList<>();
    private List<String> childrenList3 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expandable_list_view_test);

        ExpandableListView mListView = findViewById(R.id.expandableListView);
        initialData();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ClickExpandableListViewAdapter mAdapter = new ClickExpandableListViewAdapter(this, mDataSet, parentList);
        mListView.setAdapter(mAdapter);
        mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view,
                                        int parentPos, int childPos, long l) {
                Toast.makeText(ClickExpandableListViewTestActivity.this,
                        mDataSet.get(parentList[parentPos]).get(childPos), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String content = "";
                if ((int) view.getTag(R.layout.child_item) == -1) {
                    content = "父类第" + view.getTag(R.layout.parent_item) + "项" + "被长按了";
                } else {
                    content = "父类第" + view.getTag(R.layout.parent_item) + "项" + "中的"
                            + "子类第" + view.getTag(R.layout.child_item) + "项" + "被长按了";
                }
                Toast.makeText(ClickExpandableListViewTestActivity.this, content, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        mListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                return false;
            }
        });
    }

    /**
     * 初始化数据
     */
    private void initialData() {
        childrenList1.add(parentList[0] + "-" + "first");
        childrenList1.add(parentList[0] + "-" + "second");
        childrenList1.add(parentList[0] + "-" + "third");
        childrenList2.add(parentList[1] + "-" + "first");
        childrenList2.add(parentList[1] + "-" + "second");
        childrenList2.add(parentList[1] + "-" + "third");
        childrenList3.add(parentList[2] + "-" + "first");
        childrenList3.add(parentList[2] + "-" + "second");
        childrenList3.add(parentList[2] + "-" + "third");
        mDataSet.put(parentList[0], childrenList1);
        mDataSet.put(parentList[1], childrenList2);
        mDataSet.put(parentList[2], childrenList3);
    }
}