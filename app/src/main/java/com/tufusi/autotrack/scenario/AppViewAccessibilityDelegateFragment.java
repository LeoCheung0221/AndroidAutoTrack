package com.tufusi.autotrack.scenario;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.tufusi.autotrack.R;
import com.tufusi.autotrack.R2;
import com.tufusi.autotrack.databinding.FragmentAppViewAccessibilitydelegateBinding;
import com.tufusi.autotrack.databinding.FragmentAppWindowCallbackBinding;
import com.tufusi.autotrack.ui.Click4XmlTestActivity;
import com.tufusi.autotrack.ui.ClickAdapterViewTestActivity;
import com.tufusi.autotrack.ui.ClickExpandableListViewTestActivity;
import com.tufusi.autotrack.ui.ClickTabHostTestActivity;
import com.tufusi.track.sdk.TufusiDataApi;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AppViewAccessibilityDelegateFragment extends Fragment {

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        FragmentAppViewAccessibilitydelegateBinding viewDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_app_view_accessibilitydelegate, container, false);
        viewDataBinding.setHandlers(this);
        view = viewDataBinding.getRoot();
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        this.mView = view;
        super.onViewCreated(view, savedInstanceState);

        initTextView();
        initImageView();
        initButton();
        initLambdaButton();
        initShowDialogButton();
        initShowMultiChoiceDialogButton();
        initCheckBox();
        initRadioButton();
        initRadioGroup();
        initToggleButton();
        initSwitchCompat();
        initRatingBar();
        initSeekBar();
        initSpinner();
        initAdapterViewTest();
        initExpandableListViewTest();
        initTabHostButtonTest();
    }

    @OnClick(R2.id.butterKnife)
    public void butterKnifeButtonOnClick(View view) {
        showToast("Butter Knife OnClick");
    }

    @OnClick(R2.id.xmlOnClick)
    public void xmlOnClick(View view) {
        startActivity(new Intent(getContext(), Click4XmlTestActivity.class));
    }

    /**
     * 通过 DataBinding 绑定点击事件
     */
    public void dataBindingOnClick(View view) {
        showToast("DataBinding Onclick");
    }

    private void initTextView() {
        AppCompatTextView textView = mView.findViewById(R.id.textView);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void initImageView() {
        AppCompatImageView imageView = mView.findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    /**
     * 普通 setOnClickListener
     */
    private void initButton() {
        AppCompatButton button = mView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("普通按钮点击事件");
            }
        });

        registerForContextMenu(button);
    }

    /**
     * lambda 语法点击事件监听
     */
    private void initLambdaButton() {
        AppCompatButton button = mView.findViewById(R.id.lambdaButton);
        button.setOnClickListener(view -> showToast("Lambda OnClick"));
    }

    private void initShowDialogButton() {
        AppCompatButton button = mView.findViewById(R.id.showDialogButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(getActivity());
            }
        });
    }

    private void initShowMultiChoiceDialogButton() {
        AppCompatButton button = mView.findViewById(R.id.showMultiChoiceDialogButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMultiChoiceDialog(getActivity());
            }
        });
    }

    private void initCheckBox() {
        AppCompatCheckBox checkBox = mView.findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
    }

    private void initRadioButton() {
        RadioButton radioButton = mView.findViewById(R.id.radioButton);
        radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
    }

    private void initRadioGroup() {
        RadioGroup radioGroup = mView.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

            }
        });
    }

    private void initToggleButton() {
        ToggleButton toggleButton = mView.findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
    }

    private void initSwitchCompat() {
        SwitchCompat switchCompat = mView.findViewById(R.id.switchCompat);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
    }

    private void initRatingBar() {
        RatingBar ratingBar = mView.findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

            }
        });
    }

    private void initSeekBar() {
        SeekBar seekBar = mView.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initSpinner() {
        Spinner spinner = mView.findViewById(R.id.spinner);
        List<String> dataList = new ArrayList<>();
        dataList.add("条目一");
        dataList.add("条目二");
        dataList.add("条目三");
        dataList.add("条目四");
        dataList.add("条目五");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, dataList);

        //为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //为spinner绑定我们定义好的数据适配器
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {


            }
        });
    }

    private void initAdapterViewTest() {
        AppCompatButton button = mView.findViewById(R.id.adapterViewTest);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ClickAdapterViewTestActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initExpandableListViewTest() {
        AppCompatButton button = mView.findViewById(R.id.expandableListViewTest);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ClickExpandableListViewTestActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initTabHostButtonTest() {
        AppCompatButton button = mView.findViewById(R.id.tabHostButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ClickTabHostTestActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showDialog(Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("标题");
        builder.setMessage("内容");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        TufusiDataApi.getInstance().trackDialog(context, dialog);

        dialog.show();
    }

    private void showMultiChoiceDialog(Activity context) {
        Dialog dialog;
        boolean[] selected = new boolean[]{true, true, true, true, true};
        CharSequence[] items = {"多选项一", "多选项二", "多选项三", "多选项四", "多选项五"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("测试多选弹窗埋点");
        DialogInterface.OnMultiChoiceClickListener multiListener =
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface,
                                        int which, boolean isChecked) {
                        selected[which] = isChecked;
                    }
                };
        builder.setMultiChoiceItems(items, selected, multiListener);
        dialog = builder.create();
        TufusiDataApi.getInstance().trackDialog(context, dialog);
        dialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}