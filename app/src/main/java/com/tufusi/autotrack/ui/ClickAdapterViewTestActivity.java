package com.tufusi.autotrack.ui;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.tufusi.autotrack.R;
import com.tufusi.autotrack.adapter.ClickListViewAdapter;

public class ClickAdapterViewTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adapter_view_test);

        initListView();
        initGridView();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @SuppressWarnings("Convert2Lambda")
    private void initGridView() {
        GridView gridView = findViewById(R.id.gridView);
        BaseAdapter adapter = new ClickListViewAdapter(this);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ClickAdapterViewTestActivity.this, "dd：" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressWarnings("Convert2Lambda")
    private void initListView() {
        ListView listView = findViewById(R.id.listView);
        BaseAdapter adapter = new ClickListViewAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ClickAdapterViewTestActivity.this, "dd：" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}