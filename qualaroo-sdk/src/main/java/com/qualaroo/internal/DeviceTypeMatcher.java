package com.qualaroo.internal;

import android.content.Context;

import com.qualaroo.internal.model.Survey;

import java.util.List;

public class DeviceTypeMatcher {

    private static final String TYPE_PHONE = "phone";
    private static final String TYPE_TABLET = "tablet";
    private static final String TYPE_DESKTOP = "desktop";

    private final DeviceTypeProvider deviceTypeProvider;

    public DeviceTypeMatcher(DeviceTypeProvider deviceTypeProvider) {
        this.deviceTypeProvider = deviceTypeProvider;
    }

    public boolean doesDeviceMatch(Survey survey) {
        List<String> deviceTypes = survey.spec().requireMap().deviceTypeList();
        if (deviceTypes == null || deviceTypes.isEmpty()) {
            return false;
        }
        return deviceTypes.contains(deviceTypeProvider.deviceType());
    }

    interface DeviceTypeProvider {
        String deviceType();
    }

    public static class AndroidDeviceTypeProvider implements DeviceTypeProvider {

        private final static int SMALLEST_DP_FOR_TABLET = 600;
        private final Context context;

        public AndroidDeviceTypeProvider(Context context) {
            this.context = context;
        }

        @Override public String deviceType() {
            boolean isTablet = context.getResources().getConfiguration().smallestScreenWidthDp >= SMALLEST_DP_FOR_TABLET;
            return isTablet ? TYPE_TABLET : TYPE_PHONE;
        }
    }
}
