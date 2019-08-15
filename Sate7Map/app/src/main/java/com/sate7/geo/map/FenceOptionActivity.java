package com.sate7.geo.map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.google.android.material.tabs.TabLayout;
import com.sate7.geo.map.bean.Sate7Fence;
import com.sate7.geo.map.db.FenceDB;
import com.sate7.geo.map.util.XLog;
import com.zhouyou.view.seekbar.SignSeekBar;

import java.util.ArrayList;
import java.util.HashSet;

import me.shaohui.bottomdialog.BottomDialog;

public class FenceOptionActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private NumberPicker mStartHourPicker;
    private NumberPicker mEndHourPicker;
    private NumberPicker mStartMinutePicker;
    private NumberPicker mEndMinutePicker;
    private NumberPicker.Formatter mPickerFormatter;
    private EditText mETFenceName;
    private TextView mMonitorTimeStart;
    private TextView mMonitorTimeEnd;
    private EditText mCircleRadius;
    private SignSeekBar mSeekBar;
    private RadioButton mRadioMonitorIn;
    private RadioButton mRadioMonitorOut;
    private RadioButton mRadioMonitorInOut;
    private RadioButton mRadioFenceCircle;
    private RadioButton mRadioFencePolygon;
    private RadioButton mRadioFenceSelf;
    private Button mBtSave;
    private Button mBtCancel;
    private final boolean justDebug = false;
    private FenceDB mFenceDB;
    private HashSet<String> mFenceSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fence_option);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.title_create_fence);
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        initViews();
        mFenceDB = new FenceDB(this);
        mFenceSet = mFenceDB.getAllFenceName();
        XLog.dFenceDB("fences list = " + mFenceSet);
        mEditFence = getIntent().getParcelableExtra("fence");
        XLog.d("option ... fence == " + mEditFence);
        if (mEditFence != null) {
            editFence(mEditFence);
            mFenceSet.remove(mEditFence.getFenceName());
        }

        mETFenceName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                XLog.dFenceDB("beforeTextChanged " + s + "," + start + "," + count + "," + after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                XLog.dFenceDB("onTextChanged " + s + "," + start + "," + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                XLog.dFenceDB("afterTextChanged " + s + "," + mFenceSet);
                if (mFenceSet.contains(s.toString())) {
                    XLog.dFenceDB("Error");
                    mETFenceName.setError(getResources().getString(R.string.fence_type_error_exist));
                }

            }
        });
    }

    private Sate7Fence mEditFence;

    private void editFence(Sate7Fence fence) {
        XLog.d("editFence ... " + fence);
        getSupportActionBar().setTitle(R.string.title_edite_fence);
        mETFenceName.setText(fence.getFenceName());
        int mode = fence.getMonitorMode();
        switch (mode) {
            case Sate7Fence.MONITOR_MODE_IN:
                mRadioMonitorIn.setChecked(true);
                break;
            case Sate7Fence.MONITOR_MODE_OUT:
                mRadioMonitorOut.setChecked(true);
                break;
            case Sate7Fence.MONITOR_MODE_IN_OUT:
                mRadioMonitorInOut.setChecked(true);
                break;
        }
        mFenceMonitorMode = mode;
        mMonitorTimeStart.setText(getResources().getString(R.string.fence_mode_time_start_format, fence.getMonitorStartHour(), fence.getMonitorStartMinute()));
        mMonitorTimeEnd.setText(getResources().getString(R.string.fence_mode_time_end_format, fence.getMonitorEndHour(), fence.getMonitorEndMinute()));
        mStartHourPicker.setValue(fence.getMonitorStartHour());
        mStartMinutePicker.setValue(fence.getMonitorStartMinute());
        mEndHourPicker.setValue(fence.getMonitorEndHour());
        mEndMinutePicker.setValue(fence.getMonitorEndMinute());
        findViewById(R.id.radioGroup2).setVisibility(View.INVISIBLE);
        mBtSave.setText(R.string.save);
        mCircleRadius.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(0, R.anim.exit_left_to_right);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, R.anim.exit_left_to_right);
    }

    private void initViews() {
        mETFenceName = findViewById(R.id.editTextFenceName);
        mStartHourPicker = findViewById(R.id.pickerStartHour);
        mStartMinutePicker = findViewById(R.id.pickerStartMinute);
        mEndHourPicker = findViewById(R.id.pickerEndHour);
        mEndMinutePicker = findViewById(R.id.pickerEndMinute);
        mStartHourPicker.setMaxValue(23);
        mStartHourPicker.setMinValue(0);
        mStartMinutePicker.setMaxValue(59);
        mStartMinutePicker.setMinValue(0);

        mStartHourPicker.setValue(getResources().getInteger(R.integer.default_start_monitor_hour));
        mStartMinutePicker.setValue(getResources().getInteger(R.integer.default_start_monitor_minute));

        mEndHourPicker.setMaxValue(23);
        mEndHourPicker.setMinValue(0);
        mEndMinutePicker.setMaxValue(59);
        mEndMinutePicker.setMinValue(0);
        mEndHourPicker.setValue(getResources().getInteger(R.integer.default_end_monitor_hour));
        mEndMinutePicker.setValue(getResources().getInteger(R.integer.default_end_monitor_minute));

        mPickerFormatter = new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%02d", value);
            }
        };
        mStartHourPicker.setFormatter(mPickerFormatter);
        mStartMinutePicker.setFormatter(mPickerFormatter);
        mEndHourPicker.setFormatter(mPickerFormatter);
        mEndMinutePicker.setFormatter(mPickerFormatter);

        mStartHourPicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        mStartMinutePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        mEndHourPicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        mEndMinutePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);

        mStartHourPicker.setOnValueChangedListener(this);
        mStartMinutePicker.setOnValueChangedListener(this);
        mEndHourPicker.setOnValueChangedListener(this);
        mEndMinutePicker.setOnValueChangedListener(this);

        mMonitorTimeStart = findViewById(R.id.monitorStart);
        mMonitorTimeEnd = findViewById(R.id.monitorEnd);

        mCircleRadius = findViewById(R.id.fence_circle_radius);
        mSeekBar = findViewById(R.id.fence_polygon_points_seek);

        mRadioMonitorIn = findViewById(R.id.fence_mode_in);
        mRadioMonitorOut = findViewById(R.id.fence_mode_out);
        mRadioMonitorInOut = findViewById(R.id.fence_mode_in_out);
        mRadioFenceCircle = findViewById(R.id.fence_circle);
        mRadioFencePolygon = findViewById(R.id.fence_polygon);
        mRadioFenceSelf = findViewById(R.id.fence_self);
        mRadioMonitorIn.setOnCheckedChangeListener(this);
        mRadioMonitorOut.setOnCheckedChangeListener(this);
        mRadioMonitorInOut.setOnCheckedChangeListener(this);
        mRadioFenceCircle.setOnCheckedChangeListener(this);
        mRadioFencePolygon.setOnCheckedChangeListener(this);
        mRadioFenceSelf.setOnCheckedChangeListener(this);
        mBtSave = findViewById(R.id.fence_sure);
        mBtCancel = findViewById(R.id.fence_cancel);
        mBtSave.setOnClickListener(this);
        mBtCancel.setOnClickListener(this);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        switch (picker.getId()) {
            case R.id.pickerStartHour:
                mMonitorTimeStart.setText(getResources().getString(R.string.fence_mode_time_start_format, newVal, mStartMinutePicker.getValue()));
                break;
            case R.id.pickerStartMinute:
                mMonitorTimeStart.setText(getResources().getString(R.string.fence_mode_time_start_format, mStartHourPicker.getValue(), newVal));
                break;
            case R.id.pickerEndHour:
                mMonitorTimeEnd.setText(getResources().getString(R.string.fence_mode_time_end_format, newVal, mEndMinutePicker.getValue()));
                break;
            case R.id.pickerEndMinute:
                mMonitorTimeEnd.setText(getResources().getString(R.string.fence_mode_time_end_format, mEndHourPicker.getValue(), newVal));
                break;
            default:
                break;
        }
    }

    private int mFenceMonitorMode = Sate7Fence.MONITOR_MODE_IN_OUT;
    private int mFenceShape = Sate7Fence.FENCE_TYPE_CIRCLE;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        XLog.d("onCheckedChanged ww ... " + isChecked + "," + buttonView.getText());
        switch (buttonView.getId()) {
            case R.id.fence_mode_in:
                if (isChecked) {
                    mFenceMonitorMode = Sate7Fence.MONITOR_MODE_IN;
                }
                break;
            case R.id.fence_mode_out:
                if (isChecked) {
                    mFenceMonitorMode = Sate7Fence.MONITOR_MODE_OUT;
                }
                break;
            case R.id.fence_mode_in_out:
                if (isChecked) {
                    mFenceMonitorMode = Sate7Fence.MONITOR_MODE_IN_OUT;
                }
                break;
            case R.id.fence_circle:
                if (isChecked) {
                    mFenceShape = Sate7Fence.FENCE_TYPE_CIRCLE;
                    mRadioFenceSelf.setChecked(false);
                }
                showCircle(isChecked);
                break;
            case R.id.fence_polygon:
                if (isChecked) {
                    mFenceShape = Sate7Fence.FENCE_TYPE_POLYGON;
                    mRadioFenceSelf.setChecked(false);
                }
                showCircle(!isChecked);
                break;
            case R.id.fence_self:
                XLog.d("onCheckedChanged tt ... " + isChecked + "," + buttonView.getText());
                if (isChecked) {
                    mCircleCenter = null;
                    bottomDialog.show();
                }
                break;
            default:
                break;
        }
    }

    public void showCircle(boolean show) {
        XLog.d("showCircle ... " + show + "," + mRadioFenceSelf.isChecked());
        if (mRadioFenceSelf.isChecked()) {
            mCircleRadius.setVisibility(View.GONE);
            mSeekBar.setVisibility(View.GONE);
            return;
        }
        if (show) {
            mCircleRadius.setVisibility(View.VISIBLE);
            mSeekBar.setVisibility(View.GONE);
        } else {
            mSeekBar.setVisibility(View.VISIBLE);
            mCircleRadius.setVisibility(View.GONE);
            mCircleRadius.setText("");
        }
    }

    private void readPolygonLongLat(ViewGroup container) {
        mPolygonPointsList.clear();
        int count = container.getChildCount();
        View view;
        String longitude;
        String latitude;
        for (int i = 0; i < count; i++) {
            view = container.getChildAt(i);
            if (view instanceof TableRow) {
                ViewGroup viewGroup = (ViewGroup) view;
                longitude = ((EditText) viewGroup.getChildAt(0)).getText().toString();
                latitude = ((EditText) viewGroup.getChildAt(1)).getText().toString();
                XLog.d("addPolygonLongLat ... " + longitude + "," + latitude);
                if (!TextUtils.isEmpty(longitude) && !TextUtils.isEmpty(latitude)) {
                    mPolygonPointsList.add(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)));
                }
            }
        }
        XLog.d("addPolygonLongLat .. " + mPolygonPointsList);
    }

    private ViewGroup mCurrentLongLatContainer;
    private LatLng mCircleCenter;
    private ArrayList<LatLng> mPolygonPointsList = new ArrayList<>();
    private View.OnClickListener mLatClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            XLog.d("bindView onClick ...");
            bottomDialog.dismiss();
//            getLongLats();
        }
    };
    private TabLayout mTabTitle;
    private ViewPager mViewPager;
    private SelfLongLatAdapter mSelfAdapter;
    private BottomDialog bottomDialog = BottomDialog.create(getSupportFragmentManager()).setLayoutRes(
            R.layout.self_input_longlat).setViewListener(new BottomDialog.ViewListener() {
        @Override
        public void bindView(View v) {
            XLog.d("bindView ...");
            v.findViewById(R.id.longlat_ok).setOnClickListener(mLatClickListener);
            mCurrentLongLatContainer = (ViewGroup) v;
            mTabTitle = v.findViewById(R.id.self_tabs);
            mViewPager = v.findViewById(R.id.self_longlat_view_pager);
            mSelfAdapter = new SelfLongLatAdapter(FenceOptionActivity.this);
            mTabTitle.setupWithViewPager(mViewPager);
            mViewPager.setAdapter(mSelfAdapter);
        }
    });


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fence_sure:
                if (mEditFence != null) {
                    handleEditedData();
                    return;
                }
                if (justDebug || essentialInfoCompletion()) {
                    setResult(Activity.RESULT_OK, passData());
                    finish();
                    overridePendingTransition(0, R.anim.act_exit_up);
                }
                break;
            case R.id.fence_cancel:
                finish();
                overridePendingTransition(0, R.anim.act_exit_up);
                break;
            default:
                break;
        }
    }

    private boolean essentialInfoCompletion() {
        StringBuilder builder = new StringBuilder();
        if (TextUtils.isEmpty(mETFenceName.getText())) {
            builder.append(getResources().getString(R.string.fence_name_error));
            mETFenceName.requestFocus();
        }

        if (!mRadioFenceSelf.isChecked() && mFenceShape == Sate7Fence.FENCE_TYPE_CIRCLE && TextUtils.isEmpty(mCircleRadius.getText())) {
            builder.append(getResources().getString(R.string.fence_radius_error));
            mCircleRadius.requestFocus();
        }

        int start = mStartHourPicker.getValue() * 60 + mStartMinutePicker.getValue();
        int end = mEndHourPicker.getValue() * 60 + mEndMinutePicker.getValue();
        if (start >= end) {
            builder.append(getResources().getString(R.string.fence_monitor_error));
            mStartHourPicker.requestFocus();
            Toast.makeText(this, getResources().getString(R.string.fence_monitor_error), Toast.LENGTH_LONG).show();
        }

        XLog.d("essentialInfoCompletion ... " + builder.toString() + "," + mFenceShape);
        return builder.length() == 0;
    }

    private void handleEditedData() {
        if (mETFenceName.getText().toString().equals(mEditFence.getFenceName()) &&
                mFenceMonitorMode == mEditFence.getMonitorMode() &&
                mEditFence.getMonitorStartHour() == mStartHourPicker.getValue() &&
                mEditFence.getMonitorStartMinute() == mStartMinutePicker.getValue() &&
                mEditFence.getMonitorEndHour() == mEndHourPicker.getValue() &&
                mEditFence.getMonitorEndMinute() == mEndMinutePicker.getValue()) {
            XLog.d("change nothing ... ");
            Toast.makeText(this,"Nothing changed",Toast.LENGTH_SHORT).show();
            finish();
            overridePendingTransition(0, R.anim.act_exit_up);
        }

        String newName = mETFenceName.getText().toString();
        if (TextUtils.isEmpty(newName)) {
            mETFenceName.setError(getResources().getString(R.string.fence_name_error));
        }
        boolean remove = mFenceSet.remove(mEditFence.getFenceName());
        XLog.d("after remove == " + remove + " , " + mFenceSet);
        if (mFenceSet.contains(newName)) {
            mETFenceName.setError(getResources().getString(R.string.fence_name_exist_error));
        }
        mEditFence.setMonitorMode(mFenceMonitorMode);
        mEditFence.setMonitorStartHour(mStartHourPicker.getValue());
        mEditFence.setMonitorStartMinute(mStartMinutePicker.getValue());
        mEditFence.setMonitorEndHour(mEndHourPicker.getValue());
        mEditFence.setMonitorEndMinute(mEndMinutePicker.getValue());
        Intent intent = new Intent(this,MapActivity.class);
        intent.putExtra("edit_fence", mEditFence);
        intent.putExtra("old_name",mEditFence.getFenceName());
        mEditFence.setFenceName(mETFenceName.getText().toString());
        startActivity(intent);
        finish();
        overridePendingTransition(0, R.anim.act_exit_up);
    }

    private Intent passData() {
        Sate7Fence fence = new Sate7Fence(justDebug ? "MyDebugFence" : mETFenceName.getText().toString());
        fence.setMonitorMode(mFenceMonitorMode);
        fence.setMonitorStartHour(mStartHourPicker.getValue());
        fence.setMonitorStartMinute(mStartMinutePicker.getValue());
        fence.setMonitorEndHour(mEndHourPicker.getValue());
        fence.setMonitorEndMinute(mEndMinutePicker.getValue());
        fence.setFenceShape(mFenceShape);
        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        XLog.d("passData ..." + mRadioFenceSelf.isChecked());
        if (mRadioFenceSelf.isChecked()) {//self
            bundle.putBoolean("SELF_DATA", true);
            ViewGroup currentView = (ViewGroup) mSelfAdapter.getCurrentView(mViewPager.getCurrentItem());
            if (mViewPager.getCurrentItem() == 0) {//circle info
                String circleLong = ((EditText) currentView.findViewById(R.id.self_circleLong)).getText().toString();
                String circleLat = ((EditText) currentView.findViewById(R.id.self_circleLat)).getText().toString();
                XLog.d("passData circle ... " + circleLong + "," + circleLat);
                fence.setFenceShape(Sate7Fence.FENCE_TYPE_CIRCLE);
                fence.setFenceCenterLat(Double.parseDouble(circleLat));
                fence.setFenceCenterLng(Double.parseDouble(circleLong));
            } else if (mViewPager.getCurrentItem() == 1) {//polygon info
                readPolygonLongLat(currentView);
                intent.setAction("com.wlj.nani");
                fence.setFenceShape(Sate7Fence.FENCE_TYPE_POLYGON);
                fence.setFencePolygonPoints(mPolygonPointsList.size());
                XLog.d("self polygon 11 ... " + mPolygonPointsList);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng point : mPolygonPointsList) {
                    fence.addPolygonPointLatLng(point);
                    builder.include(point);
                }
                LatLng center = builder.build().getCenter();
                fence.setFenceCenterLng(center.longitude);
                fence.setFenceCenterLat(center.latitude);
            }
        } else {
            if (mFenceShape == Sate7Fence.FENCE_TYPE_CIRCLE) {
                fence.setFenceCircleRadius(justDebug ? 100 : Integer.parseInt(mCircleRadius.getText().toString()));
            } else if (mFenceShape == Sate7Fence.FENCE_TYPE_POLYGON) {
                fence.setFencePolygonPoints(mSeekBar.getProgress());
            } else {
                throw new RuntimeException("" + getResources().getString(R.string.fence_type_error));
            }
        }
        bundle.putParcelable("DATA_FENCE", fence);
        intent.putExtras(bundle);
        XLog.d("before onResult: " + fence);
        return intent;
    }

    private class SelfLongLatAdapter extends PagerAdapter {
        private ArrayList<View> selfCreateView = new ArrayList<>();

        public SelfLongLatAdapter(Context context) {
            selfCreateView.add(LayoutInflater.from(context).inflate(R.layout.self_intput_longlat_circle, null));
            selfCreateView.add(LayoutInflater.from(context).inflate(R.layout.selfinput_fence_longlat_polygon, null));
        }

        public View getCurrentView(int item) {
            return selfCreateView.get(item);
        }

        @Override
        public int getCount() {
            return selfCreateView.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(selfCreateView.get(position));
            return selfCreateView.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0 ? getResources().getString(R.string.fence_circle) : getResources().getString(R.string.fence_polygon);
        }
    }
}
