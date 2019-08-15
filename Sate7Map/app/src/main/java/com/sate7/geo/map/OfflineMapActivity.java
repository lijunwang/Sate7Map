package com.sate7.geo.map;

import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.PhoneUtils;
import com.sate7.geo.map.util.XLog;
import com.yanzhenjie.recyclerview.OnItemMenuClickListener;
import com.yanzhenjie.recyclerview.SwipeMenu;
import com.yanzhenjie.recyclerview.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import java.util.ArrayList;
import java.util.List;

import static com.baidu.mapapi.map.offline.MKOfflineMap.TYPE_NETWORK_ERROR;


public class OfflineMapActivity extends AppCompatActivity implements MKOfflineMapListener {
    private SwipeRecyclerView mRecyclerView;
    private MyOfflineMapAdapter mMapAdapter;
    private RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    private RecyclerView.ItemDecoration itemDecoration = new MyDecoration();
    private MKOfflineMap offlineMap;
    private ArrayList<MKOLUpdateElement> hasDownloadedCityLists;
    private ArrayList<MKOLSearchRecord> allOfflineCityLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_map);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.title_offline_map);
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        initViews();
        boolean wifi = NetworkUtils.isWifiAvailable();
        boolean fourG = NetworkUtils.is4G();
        XLog.dOffline("wifi == " + wifi + ",fourG == " + fourG + ",isConnected == " + NetworkUtils.isConnected());
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        finish();
        overridePendingTransition(0, R.anim.exit_left_to_right);
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

    private OnItemMenuClickListener onItemMenuClickListener = new OnItemMenuClickListener() {
        @Override
        public void onItemClick(SwipeMenuBridge menuBridge, int adapterPosition) {
            menuBridge.closeMenu();
            int direction = menuBridge.getDirection(); // 左侧还是右侧菜单。
            int menuPosition = menuBridge.getPosition(); // 菜单在RecyclerView的Item中的Position。
            XLog.dOffline("onItemMenuClickListener ... " + direction + "," + menuPosition + "," + adapterPosition);
        }
    };
    private SwipeMenuCreator creator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int position) {
            SwipeMenuItem deleteItem = new SwipeMenuItem(OfflineMapActivity.this).setBackground(
                    R.drawable.selector_red)
                    .setImage(R.mipmap.ic_action_delete)
                    .setText("删除")
                    .setTextColor(Color.WHITE)
                    .setWidth(200)
                    .setHeight(getResources().getDimensionPixelSize(R.dimen.item_height));
            rightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。
            leftMenu.addMenuItem(deleteItem);
        }
    };

    private void initViews() {
        offlineMap = new MKOfflineMap();
        offlineMap.init(this);
        hasDownloadedCityLists = offlineMap.getAllUpdateInfo();
        allOfflineCityLists = offlineMap.getOfflineCityList();
        if (hasDownloadedCityLists != null) {
            for (MKOLUpdateElement element : hasDownloadedCityLists) {
                XLog.dOffline("MKOLUpdateElement ... cityName = " + element.cityName + ",level = " + element.level + ", update = " + element.update + ", cityID = " + element.cityID);
            }
        }
        ArrayList<MKOLSearchRecord> child;
        for (MKOLSearchRecord record : allOfflineCityLists) {
            XLog.dOffline("MKOLSearchRecord ... cityType = " + record.cityType + ",cityID = " + record.cityID + ",cityName = " + record.cityName);
            child = record.childCities;
            if (child != null) {
                for (MKOLSearchRecord r : record.childCities) {
                    XLog.dOffline("MKOLSearchRecord child ... " + r.cityType + "," + r.cityID + "," + r.cityName);
                }
            }

        }

        mRecyclerView = findViewById(R.id.offlineRecyclerView);
        mMapAdapter = new MyOfflineMapAdapter(this, allOfflineCityLists, hasDownloadedCityLists);
        layoutManager = new LinearLayoutManager(this);
//        mRecyclerView.setSwipeMenuCreator(creator);
        mRecyclerView.setOnItemMenuClickListener(onItemMenuClickListener);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mMapAdapter);
        mRecyclerView.addItemDecoration(itemDecoration);
    }

    private boolean downloading = false;
    private int toDownAfterBase = -1;
    private int downloadingCityId = -1;
    private String updateInfo;

    private AlertDialog wifiAlertDialog;

    private void showNoWifiDialog(final MKOLSearchRecord city) {
        if (wifiAlertDialog == null) {
            wifiAlertDialog = new AlertDialog.Builder(this).
                    setTitle(R.string.map_download).
                    setMessage(R.string.start_no_wifi).
                    setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            XLog.d("onClick setNegativeButton ... ");
                        }
                    }).
                    setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            XLog.d("onClick setNegativeButton ... ");
                            Toast.makeText(OfflineMapActivity.this, getResources().getString(R.string.start_download, city.cityName), Toast.LENGTH_LONG).show();
                            offlineMap.start(city.cityID);
                        }
                    }).create();
        }

        wifiAlertDialog.show();
    }

    @Override
    public void onGetOfflineMapState(int type, int state) {
        if (type == MKOfflineMap.TYPE_DOWNLOAD_UPDATE) {
            MKOLUpdateElement element = offlineMap.getUpdateInfo(state);
            if (element != null) {
                downloadingCityId = element.cityID;
                downloading = true;
                updateInfo = getResources().getString(R.string.down_progress, element.cityName, element.ratio) + "%";
                mMapAdapter.notifyDataSetChanged();
                XLog.dOffline("onGetOfflineMapState bb ... " + element.cityID + "," + element.cityName + "," + element.ratio + "," + updateInfo);
            } else {
                XLog.e("onGetOfflineMapState element == null ");
            }
        } else if (type == TYPE_NETWORK_ERROR) {
            Toast.makeText(this, R.string.net_error, Toast.LENGTH_LONG).show();
        }
    }

    private class MyOfflineMapAdapter extends RecyclerView.Adapter {
        private OfflineMapActivity mContext;
        private ArrayList<MKOLSearchRecord> mCityLists;
        private ArrayList<MKOLUpdateElement> mHasDownloaded;
        private SparseBooleanArray sparseBooleanArray = new SparseBooleanArray();

        public MyOfflineMapAdapter(OfflineMapActivity context, ArrayList<MKOLSearchRecord> city, ArrayList<MKOLUpdateElement> hasDownloaded) {
            mContext = context;
            mCityLists = city;
            mHasDownloaded = hasDownloaded;
            //find downloads;
            findProvinceDownloaded();
            XLog.dOffline("sparseBooleanArray == " + sparseBooleanArray);
        }

        private void findProvinceDownloaded() {
            ArrayList<MKOLSearchRecord> child;
            for (MKOLSearchRecord record : mCityLists) {//2 level
                if (mHasDownloaded != null) {
                    for (MKOLUpdateElement element : mHasDownloaded) {
                        if (element.cityID == record.cityID) {
                            XLog.dOffline("has download ... " + element.cityName);
                            sparseBooleanArray.put(element.cityID, true);
                        }
                    }
                }
                child = record.childCities;
                if (child != null) {
                    sparseBooleanArray.put(record.cityID, true);
                    for (MKOLSearchRecord r : record.childCities) {//1 level
                        MKOLUpdateElement element = offlineMap.getUpdateInfo(r.cityID);
                        XLog.dOffline("MKOLSearchRecord child ...cityType = " + r.cityType + ",cityID = " + r.cityID + ",cityName = " + r.cityName + ",radio = " + (element == null ? " null " : element.ratio));
                        if (element == null || element.ratio != 100) {
                            sparseBooleanArray.put(record.cityID, false);
                            break;
                        }
                    }
                }
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            return new OfflineMapListHolder(LayoutInflater.from(mContext).inflate(R.layout.offline_city_list_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
            OfflineMapListHolder listHolder = (OfflineMapListHolder) viewHolder;
            MKOLSearchRecord record = mCityLists.get(position);
            listHolder.name.setText(record.cityName);
            listHolder.size.setText(formatDataSize(record.dataSize));
            listHolder.imgState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!NetworkUtils.isConnected()) {
                        Toast.makeText(OfflineMapActivity.this, getResources().getString(R.string.start_no_network), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!NetworkUtils.isWifiAvailable()) {
//                        Toast.makeText(OfflineMapActivity.this, getResources().getString(R.string.start_no_wifi), Toast.LENGTH_LONG).show();
                        showNoWifiDialog(mCityLists.get(position));
                        return;
                    }
                    Toast.makeText(OfflineMapActivity.this, getResources().getString(R.string.start_download, mCityLists.get(position).cityName), Toast.LENGTH_LONG).show();
                    offlineMap.start(mCityLists.get(position).cityID);
                }
            });
            if (sparseBooleanArray.get(record.cityID)) {//dowloaded
                listHolder.imgState.setImageResource(R.mipmap.downloaded);
                listHolder.imgState.setOnClickListener(null);
            } else {
                listHolder.imgState.setImageResource(R.mipmap.to_down);
            }

            if (updateInfo != null && updateInfo.contains("全国")) {
                XLog.dOffline("onBindViewHolder ..." + downloadingCityId + "," + downloading + "," + updateInfo);
            }
            if (downloading && containsCity(record, downloadingCityId)) {
                listHolder.progress.setText(updateInfo);
                //judge download all city;
                if (childHasDownloadAll(record)) {
                    listHolder.imgState.setImageResource(R.mipmap.downloaded);
                    listHolder.imgState.setOnClickListener(null);
                    listHolder.progress.setText("");
                    sparseBooleanArray.put(record.cityID, true);
                }
            } else {
                listHolder.progress.setText("");
                XLog.dOffline("onBindViewHolder fuck ..." + updateInfo);
            }
        }


        @Override
        public int getItemCount() {
            return mCityLists.size();
        }

        public String formatDataSize(long size) {
            String ret = "";
            if (size < (1024 * 1024)) {
                ret = String.format("%dK", size / 1024);
            } else {
                ret = String.format("%.1fM", size / (1024 * 1024.0));
            }
            return ret;
        }
    }

    private boolean childHasDownloadAll(MKOLSearchRecord record) {
        if (record.cityID == 1) {
            XLog.dOffline("childHasDownloadAll ... " + record.cityName + "," + record.childCities + "," + offlineMap.getUpdateInfo(record.cityID));
        }
        ArrayList<MKOLSearchRecord> childCities = record.childCities;
        MKOLUpdateElement element;
        if (childCities != null) {
            for (MKOLSearchRecord r : childCities) {
                element = offlineMap.getUpdateInfo(r.cityID);
                if (element == null || element.ratio != 100) {
                    return false;
                }
            }
        } else {
            element = offlineMap.getUpdateInfo(record.cityID);
            if (element != null && element.ratio == 100) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean containsCity(MKOLSearchRecord record, int cityId) {
        if (cityId == 1) {
            XLog.dOffline("containsCity ..." + record.cityID + "," + record.cityName + "," + cityId + "," + record.childCities);
        }
        if (record.cityID == cityId) {
            return true;
        }
        ArrayList<MKOLSearchRecord> child = record.childCities;
        if (child == null) {
            return false;
        }
        for (MKOLSearchRecord r : child) {
            if (r.cityID == cityId) {
                return true;
            }
        }
        return false;
    }

    private static class OfflineMapListHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView size;
        private TextView progress;
        private ImageView imgState;
        private ImageView imgDelete;

        public OfflineMapListHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.offline_list_city_name);
            size = itemView.findViewById(R.id.offline_list_city_size);
            progress = itemView.findViewById(R.id.offline_list_city_progress);
            imgState = itemView.findViewById(R.id.offline_list_city_state);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class MyDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(0, 1, 0, 0);
        }

        @Override
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.onDraw(c, parent, state);
            XLog.d("MyDecoration onDraw ..." + c.getWidth() + "," + c.getHeight());
        }

        @Override
        public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.onDrawOver(c, parent, state);
            parent.getChildCount();
            XLog.d("MyDecoration onDrawOver ..." + c.getWidth() + "," + c.getHeight());
        }
    }
}
