package com.tufusi.autotrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.tufusi.track.sdk.TrackClickMode;
import com.tufusi.track.sdk.TufusiDataApi;

public class MainFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_MainFragment_to_FirstFragment);
            }
        });
        view.findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_MainFragment_to_SecondFragment);
            }
        });
        view.findViewById(R.id.button_third).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TufusiDataApi.getInstance().setTrackClickMode(TrackClickMode.CUSTOM_LISTENER);
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_MainFragment_to_ThirdFragment);
            }
        });
        view.findViewById(R.id.button_fourth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TufusiDataApi.getInstance().setTrackClickMode(TrackClickMode.WINDOW_CALLBACK);
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_MainFragment_to_FourthFragment);
            }
        });
        view.findViewById(R.id.button_fifth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TufusiDataApi.getInstance().setTrackClickMode(TrackClickMode.ACCESSIBILITY_DELEGATE);
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_MainFragment_to_FifthFragment);
            }
        });
        view.findViewById(R.id.button_sixth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TufusiDataApi.getInstance().setTrackClickMode(TrackClickMode.TRANSPARENT_LAYOUT);
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_MainFragment_to_SixthFragment);
            }
        });
        view.findViewById(R.id.button_seventh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_MainFragment_to_SeventhFragment);
            }
        });
        view.findViewById(R.id.button_eighth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_MainFragment_to_EighthFragment);
            }
        });
        view.findViewById(R.id.button_ninth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_MainFragment_to_NinthFragment);
            }
        });
        view.findViewById(R.id.button_tenth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(MainFragment.this)
                        .navigate(R.id.action_MainFragment_to_TenthFragment);
            }
        });
    }

}