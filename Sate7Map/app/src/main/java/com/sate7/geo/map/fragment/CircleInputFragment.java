package com.sate7.geo.map.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sate7.geo.map.R;

public class CircleInputFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
//        return LayoutInflater.from(getContext()).inflate(R.layout.activity_fence_option, container, true);
        TextView textView = new TextView(getContext());
        textView.setText("Fuck");
        return textView;
    }
}
