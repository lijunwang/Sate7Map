package com.sate7.geo.map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sate7.geo.map.bean.Sate7Fence;
import com.sate7.geo.map.bean.Sate7Track;
import com.sate7.geo.map.db.FenceDB;
import com.sate7.geo.map.util.XLog;
import com.yanzhenjie.recyclerview.OnItemClickListener;
import com.yanzhenjie.recyclerview.OnItemLongClickListener;
import com.yanzhenjie.recyclerview.OnItemMenuClickListener;
import com.yanzhenjie.recyclerview.SwipeMenu;
import com.yanzhenjie.recyclerview.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import java.util.ArrayList;

public class FenceListActivity extends AppCompatActivity implements OnItemClickListener {
    private SwipeRecyclerView mRecyclerView;
    private FenceAdapter mFenceAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean isTrack = false;
    private SwipeMenuCreator creator1 = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int position) {
            SwipeMenuItem deleteItem = new SwipeMenuItem(FenceListActivity.this).setBackground(
                    R.drawable.selector_red)
                    .setImage(R.mipmap.ic_action_delete)
                    .setText(getResources().getString(R.string.delete))
                    .setTextColor(Color.WHITE)
                    .setWidth(200)
                    .setHeight(getResources().getDimensionPixelSize(R.dimen.item_height));
            rightMenu.addMenuItem(deleteItem);
        }
    };
    private SwipeMenuCreator creator2 = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int position) {
            SwipeMenuItem deleteItem = new SwipeMenuItem(FenceListActivity.this).setBackground(
                    R.drawable.selector_red)
                    .setImage(R.mipmap.ic_action_delete)
                    .setText(getResources().getString(R.string.delete))
                    .setTextColor(Color.WHITE)
                    .setWidth(200)
                    .setHeight(getResources().getDimensionPixelSize(R.dimen.item_height));
            SwipeMenuItem editItem = new SwipeMenuItem(FenceListActivity.this).setBackground(
                    R.drawable.selector_green)
                    .setImage(R.mipmap.edit)
                    .setText(getResources().getString(R.string.edit))
                    .setTextColor(Color.WHITE)
                    .setWidth(200)
                    .setHeight(getResources().getDimensionPixelSize(R.dimen.item_height));
            rightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。
            rightMenu.addMenuItem(editItem);
        }
    };
    private OnItemMenuClickListener clickListener = new OnItemMenuClickListener() {
        @Override
        public void onItemClick(SwipeMenuBridge menuBridge, int adapterPosition) {
            XLog.d("clickListener ... " + adapterPosition + "," + menuBridge.getDirection() + "," + menuBridge.getPosition());
//            mFenceAdapter
            if (menuBridge.getDirection() == SwipeRecyclerView.RIGHT_DIRECTION && menuBridge.getPosition() == 0) {
                menuBridge.closeMenu();
                mFenceAdapter.deleteFence(adapterPosition);
                XLog.d("delete ...");
            }
            if (menuBridge.getDirection() == SwipeRecyclerView.RIGHT_DIRECTION && menuBridge.getPosition() == 1) {
                menuBridge.closeMenu();
//                mFenceAdapter.deleteFence(adapterPosition);
                XLog.d("edit ...");
                Intent intent = new Intent(FenceListActivity.this, FenceOptionActivity.class);
                intent.putExtra("fence", mFenceAdapter.getSateFence(adapterPosition));
                startActivity(intent);
                finish();
                overridePendingTransition(0, R.anim.exit_left_to_right);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fence_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        mDeleteFence.clear();
        isTrack = getIntent().getBooleanExtra("ListTrack", false);
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(isTrack ?
                    getResources().getString(R.string.map_query_track) :
                    getResources().getString(R.string.map_query_fence));
        }

        mRecyclerView = findViewById(R.id.recyclerView);
        mFenceAdapter = new FenceAdapter(this, isTrack);
        mRecyclerView.setSwipeMenuCreator(isTrack ? creator1 : creator2);
        mRecyclerView.setOnItemMenuClickListener(clickListener);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setOnItemClickListener(this);
        mRecyclerView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int adapterPosition) {
                XLog.d("onItemLongClick ... " + adapterPosition);
                Toast.makeText(FenceListActivity.this, R.string.left_drag, Toast.LENGTH_SHORT).show();
            }
        });
        mRecyclerView.setAdapter(mFenceAdapter);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }

            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.set(0, 2, 0, 0);
            }
        });
    }

    private ArrayList<String> mDeleteFence = new ArrayList<>();

    private void returnDeleteFenceData() {
        XLog.d("returnDeleteFenceData ..." + mDeleteFence);
        if (!mDeleteFence.isEmpty()) {
            Bundle bundle = new Bundle();
            String[] names = new String[mDeleteFence.size()];
            mDeleteFence.toArray(names);
            bundle.putStringArray("delete_fence_name", names);
            setResult(Activity.RESULT_OK, getIntent().putExtras(bundle));
        }
    }

    @Override
    public void onBackPressed() {
        returnDeleteFenceData();
        finish();
        overridePendingTransition(0, R.anim.exit_left_to_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                returnDeleteFenceData();
                finish();
                overridePendingTransition(0, R.anim.exit_left_to_right);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int adapterPosition) {
        XLog.d("onItemClick ... " + adapterPosition + "," + isTrack);
        Intent intent = getIntent();
        if(isTrack){
            intent.putExtra("show_track", true);
            intent.putExtra("track_name", mFenceAdapter.getSateTrackName(adapterPosition));
        }else{
            Sate7Fence fence = mFenceAdapter.getSateFence(adapterPosition);
            intent.putExtra("center_fence", true);
            intent.putExtra("fence", fence);
        }
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(0, R.anim.exit_left_to_right);
    }

    private class FenceAdapter extends RecyclerView.Adapter {
        private ArrayList<Sate7Fence> fenceList;
        private ArrayList<Sate7Track> trackList;
        private Resources resources;
        private boolean mIsTrack = false;
        private FenceDB fenceDB;

        public FenceAdapter(Context context, boolean isTrack) {
            resources = context.getResources();
            mIsTrack = isTrack;
            fenceDB = new FenceDB(context);
            if (mIsTrack) {
                trackList = fenceDB.listAllTrackInfo();
            } else {
                fenceList = fenceDB.queryAllFence();
            }
        }

        public void deleteFence(int position) {
            String fenceName = fenceList.get(position).getFenceName();
            int delete = fenceDB.deleteByFenceName(fenceName);
            XLog.d("deleteItem ... " + fenceName + "," + delete);
            if (delete > -1) {
                mDeleteFence.add(fenceName);
            }
            fenceList.clear();
            fenceList = fenceDB.queryAllFence();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            if (mIsTrack) {
                return new TrackViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fence_recycler_item_track, viewGroup, false));
            } else {
                return new MyViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fence_recycler_item_fence, viewGroup, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            if (mIsTrack) {
                TrackViewHolder holder = (TrackViewHolder) viewHolder;
                holder.name.setText(trackList.get(i).getName());
                holder.date.setText(trackList.get(i).getDate());
            } else {
                Sate7Fence fence = fenceList.get(i);
                MyViewHolder holder = (MyViewHolder) viewHolder;
                holder.name.setText(fence.getFenceName());
                holder.date.setText(fence.getDateInfo());
                XLog.dFenceDB("FenceAdapter fenceList date... " + fence.getDateInfo());
                holder.other.setText(buildOtherInfo(fence));
                if (fence.getMonitorMode() == Sate7Fence.MONITOR_MODE_IN_OUT) {
                    holder.mode.setText(getResources().getString(R.string.notify_type, getResources().getString(R.string.fence_mode_in_out)));
                } else if (fence.getMonitorMode() == Sate7Fence.MONITOR_MODE_IN) {
                    holder.mode.setText(getResources().getString(R.string.notify_type, getResources().getString(R.string.fence_mode_in)));
                } else if (fence.getMonitorMode() == Sate7Fence.MONITOR_MODE_OUT) {
                    holder.mode.setText(getResources().getString(R.string.notify_type, getResources().getString(R.string.fence_mode_out)));
                }
                holder.startEndTime.setText(getResources().getString(R.string.valid_time, fence.getMonitorStartHour(), fence.getMonitorStartMinute(), fence.getMonitorEndHour(), fence.getMonitorEndMinute()));
            }
        }

        @Override
        public int getItemCount() {
            return mIsTrack ? trackList.size() : fenceList.size();
        }

        public Sate7Fence getSateFence(int position) {
            if (isTrack) {
                return null;
            }
            return fenceList.get(position);
        }

        public Sate7Track getSateTrack(int position) {
            return trackList.get(position);
        }

        public String getSateTrackName(int position) {
            return trackList.get(position).getName();
        }

        private String buildOtherInfo(Sate7Fence fence) {
            if (fence.getFenceShape() == Sate7Fence.FENCE_TYPE_CIRCLE) {
                return resources.getString(R.string.describe_circle, fence.getFenceCircleRadius());
            } else if (fence.getFenceShape() == Sate7Fence.FENCE_TYPE_POLYGON) {
                return resources.getString(R.string.describe_polygon, fence.getFencePolygonPoints());
            } else {
                return "";
            }
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView date;
        TextView other;
        TextView mode;
        TextView startEndTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            date = itemView.findViewById(R.id.item_date);
            other = itemView.findViewById(R.id.item_other_info);
            mode = itemView.findViewById(R.id.item_mode);
            startEndTime = itemView.findViewById(R.id.item_valid_time);
        }
    }

    private static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView date;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.track_name);
            date = itemView.findViewById(R.id.track_time);
        }
    }

}
