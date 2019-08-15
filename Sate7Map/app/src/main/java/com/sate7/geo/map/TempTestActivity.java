package com.sate7.geo.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sate7.geo.map.fragment.CircleInputFragment;
import com.sate7.geo.map.fragment.PolygonInputFragment;
import com.sate7.geo.map.util.XLog;

import java.util.ArrayList;

import me.shaohui.bottomdialog.BottomDialog;

public class TempTestActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "TempTestActivity";
    private BottomDialog bottomDialog;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
    private ArrayList<View> test = new ArrayList<>();
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_test);
        findViewById(R.id.set).setOnClickListener(this);
        findViewById(R.id.get).setOnClickListener(this);
        test.add(LayoutInflater.from(this).inflate(R.layout.activity_fence_option,null));
        test.add(LayoutInflater.from(this).inflate(R.layout.activity_fence_option,null));
        fragmentManager = getSupportFragmentManager();
        bottomDialog = BottomDialog.create(fragmentManager).setLayoutRes(R.layout.self_input_longlat).setViewListener(new BottomDialog.ViewListener() {
            @Override
            public void bindView(View v) {
                XLog.d("bindView ....");
                viewPager = v.findViewById(R.id.self_longlat_view_pager);
                tabLayout = v.findViewById(R.id.self_tabs);
                tabLayout.setupWithViewPager(viewPager);
                viewPager.setAdapter(new PagerAdapter() {
                    @Override
                    public int getCount() {
                        XLog.d("getCount ...." + test.size());
                        return test.size();
                    }

                    @Override
                    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                        return view == object;
                    }

                    @Override
                    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                        container.removeView(test.get(position));
                        XLog.d("destroyItem ...." + position);
                    }

                    @NonNull
                    @Override
                    public Object instantiateItem(@NonNull ViewGroup container, int position) {
                        container.addView(test.get(position), 0);
                        XLog.d("instantiateItem ...." + position);
                        return test.get(position);
                    }

                    @Nullable
                    @Override
                    public CharSequence getPageTitle(int position) {
                        if(position == 0){
                            return "AAA";
                        }else{
                            return "BBB";
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.set:
                bottomDialog.show();
                break;
            case R.id.get:
                bottomDialog.dismiss();
                break;
            default:
                break;
        }
    }

}
