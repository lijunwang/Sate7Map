package com.sate7.geo.map.util;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.sate7.geo.map.R;

public class BitmapGetter {

    public static BitmapDescriptor getBitmap(int number) {
        switch (number) {
            case 1:
                return BitmapDescriptorFactory
                        .fromResource(R.mipmap.icon_mark1);
            case 2:
                return BitmapDescriptorFactory
                        .fromResource(R.mipmap.icon_mark2);
            case 3:
                return BitmapDescriptorFactory
                        .fromResource(R.mipmap.icon_mark3);
            case 4:
                return BitmapDescriptorFactory
                        .fromResource(R.mipmap.icon_mark4);
            case 5:
                return BitmapDescriptorFactory
                        .fromResource(R.mipmap.icon_mark5);
            case 6:
                return BitmapDescriptorFactory
                        .fromResource(R.mipmap.icon_mark6);
            case 7:
                return BitmapDescriptorFactory
                        .fromResource(R.mipmap.icon_mark7);
            case 8:
                return BitmapDescriptorFactory
                        .fromResource(R.mipmap.icon_mark8);
            case 9:
                return BitmapDescriptorFactory
                        .fromResource(R.mipmap.icon_mark9);
            case 10:
                return BitmapDescriptorFactory
                        .fromResource(R.mipmap.icon_mark10);
            default:
                return BitmapDescriptorFactory
                        .fromResource(R.mipmap.icon_markx);
        }
    }
}
