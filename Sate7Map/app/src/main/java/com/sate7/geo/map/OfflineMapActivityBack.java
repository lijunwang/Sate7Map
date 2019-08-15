package com.sate7.geo.map;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.sate7.geo.map.util.XLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class OfflineMapActivityBack extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView mRecyclerView;
    private MyOfflineMapAdapter mMapAdapter;
    private RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    private RecyclerView.ItemDecoration itemDecoration = new MyDecoration();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_map);
        initViews();
    }

    private void initViews() {
        mRecyclerView = findViewById(R.id.offlineRecyclerView);
        mMapAdapter = new MyOfflineMapAdapter(this);
        layoutManager = new LinearLayoutManager(this);
        layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mMapAdapter);
        mRecyclerView.addItemDecoration(itemDecoration);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                break;
        }
    }

    private static class MyOfflineMapAdapter extends RecyclerView.Adapter implements MKOfflineMapListener, View.OnClickListener {
        private OfflineMapActivityBack mContext;
        private MKOfflineMap mkOfflineMap;
        private MKOLUpdateElement updateElement;
        private ArrayList<MKOLSearchRecord> offlineList;
        private ArrayList<MKOLUpdateElement> downloadList;
        private HashMap<Integer, Boolean> mSelectCityMap = new HashMap<>();

        private String listChild(MKOLSearchRecord record) {
            StringBuilder sb = new StringBuilder();
            ArrayList<MKOLSearchRecord> childCities = record.childCities;
            sb.append("[");
            if (childCities != null) {
                for (MKOLSearchRecord r : childCities) {
                    sb.append("(id:" + r.cityID + "," + r.cityName + ")");
                }
            }
            sb.append("]");
            return sb.toString();
        }

        private int getDownCount(MKOLSearchRecord record) {
            int count = 0;
            ArrayList<MKOLSearchRecord> childCities = record.childCities;
            if (childCities != null) {
                for (MKOLSearchRecord r : childCities) {
                    XLog.dOffline("updateProgress getDownCount ... " + mkOfflineMap.getUpdateInfo(r.cityID).cityName + "," + mkOfflineMap.getUpdateInfo(r.cityID).ratio);
                    if (mkOfflineMap.getUpdateInfo(r.cityID).ratio == 100) {
                        count++;
                    }
                }
            }
            return count;
        }

        public MyOfflineMapAdapter(OfflineMapActivityBack context) {
            mkOfflineMap = new MKOfflineMap();
            mContext = context;
            mkOfflineMap.init(this);
            offlineList = mkOfflineMap.getOfflineCityList();
            downloadList = mkOfflineMap.getAllUpdateInfo();
            for (MKOLSearchRecord record : offlineList) {
                mSelectCityMap.put(record.cityID, false);
                XLog.dOffline("data debug record: " + record.cityID + "," + record.cityName + "," + record.cityType + "," + listChild(record));
            }

            if (downloadList != null) {
                for (MKOLUpdateElement element : downloadList) {
                    XLog.dOffline("data debug element: " + element.cityID + "," + element.update + "," + element.cityName + "," + element.ratio);
                }
            }
        }

        public void onDestroy() {
            mkOfflineMap.destroy();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new OfflineMapListHolder(LayoutInflater.from(mContext).inflate(R.layout.offline_city_list_item, null));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            MKOLSearchRecord record = offlineList.get(i);
            OfflineMapListHolder listHolder = (OfflineMapListHolder) viewHolder;
            listHolder.name.setText(record.cityName + "(" + record.cityID + ")");
            listHolder.size.setText(formatDataSize(record.dataSize));
            //progress
            //state
            if (hasDownloadToLocal(record)) {
                listHolder.imgState.setImageResource(R.mipmap.downloaded);
                listHolder.imgDelete.setVisibility(View.VISIBLE);
                listHolder.imgDelete.setOnClickListener(this);
                listHolder.imgDelete.setTag("" + record.cityID);
                listHolder.imgState.setOnClickListener(null);
            } else {
                listHolder.imgState.setImageResource(R.mipmap.to_down);
                listHolder.imgState.setOnClickListener(this);
                listHolder.imgState.setTag("" + record.cityID);
                listHolder.imgDelete.setVisibility(View.INVISIBLE);
                listHolder.imgDelete.setOnClickListener(null);
            }
            updateProgress(record, listHolder);
        }

        private boolean hasDownloadToLocal(MKOLSearchRecord record) {
            downloadList = mkOfflineMap.getAllUpdateInfo();
            if (downloadList == null) {
                return false;
            }
            if (record.cityType == 2 || record.cityType == 0) {
                for (MKOLUpdateElement element : downloadList) {
                    if (element.cityID == record.cityID && element.ratio == 100) {
                        XLog.dOffline("hasDownloadToLocal record.cityType == 2 || 0 ... " + record.cityType + "," + record.cityName);
                        return true;
                    }
                }
                return false;
            } else if (record.cityType == 1) {
                //判断所有的子城市是否已经下载
                HashSet<Integer> downCityIds = new HashSet<>();
                for (MKOLUpdateElement element : downloadList) {
                    downCityIds.add(element.cityID);
                }
                ArrayList<MKOLSearchRecord> childList = record.childCities;
                for (MKOLSearchRecord recordChild : childList) {
                    if (!downCityIds.contains(recordChild.cityID)) {
                        return false;
                    }
                }
                XLog.dOffline("hasDownloadToLocal record.cityType == 1 ... " + record.cityName);
                return true;
            }
            return false;
        }

        private boolean fakeProgress = false;
        private int fakeCityId = -1;

        private boolean updateProgress(MKOLSearchRecord record, OfflineMapListHolder holder) {
            if (fakeProgress && fakeCityId != -1 && record.cityID == fakeCityId) {
                XLog.dOffline("updateProgress fakeProgress ... " + record.cityID + "," + record.cityName);
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.progressBar.setMax(100);
                holder.progressBar.setProgress(1);
            }
            if (downloadList == null || updateElement == null) {
                return false;
            }
            MKOLSearchRecord updateSearchRecord = getParent(updateElement);
            XLog.dOffline("updateProgress ... " + record.cityID + "," + record.cityName);
            if (updateSearchRecord != null && updateSearchRecord.cityID == record.cityID) {
                holder.progressBar.setVisibility(View.VISIBLE);
                int childCount = updateSearchRecord.childCities.size();
                int hasDown = getDownCount(record);
                XLog.dOffline("updateProgress  AA ... " + childCount + "," + hasDown);
                holder.progressBar.setMax(childCount);
                holder.progressBar.setProgress(hasDown);
                fakeProgress = false;
                fakeCityId = -1;
                if (childCount == hasDown) {
                    Toast.makeText(mContext, R.string.download_ok, Toast.LENGTH_LONG).show();
                    holder.progressBar.setVisibility(View.INVISIBLE);
                }
            } else {
                holder.progressBar.setVisibility(View.INVISIBLE);
                XLog.dOffline("updateProgress bb ... ");
            }
            return false;
        }

        private MKOLSearchRecord getParent(MKOLUpdateElement element) {
            int cityID = element.cityID;
            for (MKOLSearchRecord record : offlineList) {
                ArrayList<MKOLSearchRecord> childCities = record.childCities;
                if (childCities != null) {
                    for (MKOLSearchRecord childRecord : childCities) {
                        if (childRecord.cityID == cityID) {
                            XLog.dOffline("updateProgress getParent aa ... " + record.cityID + "," + record.cityName);
                            return record;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return offlineList.size();
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

        @Override
        public void onGetOfflineMapState(int type, int state) {
            if (type == MKOfflineMap.TYPE_DOWNLOAD_UPDATE) {
                updateElement = mkOfflineMap.getUpdateInfo(state);
                // 处理下载进度更新提示
                if (updateElement != null && updateElement.ratio == 100) {
                    XLog.dOffline("onGetOfflineMapState bb ... " + updateElement.cityID + "," + updateElement.cityName + "," + updateElement.ratio);
                    notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.offline_list_city_state:
                    boolean start = mkOfflineMap.start(Integer.parseInt((String) view.getTag()));
                    XLog.dOffline("offline_list_city_state ... " + view.getTag() + "," + start);
                    if (start) {
                        fakeCityId = Integer.parseInt((String) view.getTag());
                        fakeProgress = true;
                        notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static class OfflineMapListHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView size;
        private ProgressBar progressBar;
        private ImageView imgState;
        private ImageView imgDelete;

        public OfflineMapListHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.offline_list_city_name);
            size = itemView.findViewById(R.id.offline_list_city_size);
            progressBar = itemView.findViewById(R.id.offline_list_city_progress);
            imgState = itemView.findViewById(R.id.offline_list_city_state);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapAdapter.onDestroy();
    }

    private class MyDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(0, 2, 0, 0);
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
