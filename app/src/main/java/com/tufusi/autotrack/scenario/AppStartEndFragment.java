package com.tufusi.autotrack.scenario;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.tufusi.autotrack.R;
import com.tufusi.autotrack.ui.AppStartEndActivity;

public class AppStartEndFragment extends Fragment implements View.OnClickListener {

    private Button jumpNewActivity;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_app_start_end, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        jumpNewActivity = view.findViewById(R.id.jump_new_activity);
        jumpNewActivity.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.jump_new_activity:
                startActivity(new Intent(getContext(), AppStartEndActivity.class));
                break;
            default:
                break;
        }
    }
}