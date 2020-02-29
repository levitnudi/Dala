package yali.org.service;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;

import yali.org.Constants;
import yali.org.provider.FeedDataContentProvider;
import yali.org.service.FetcherService;
import yali.org.utils.PrefUtils;

public class AutoRefreshService extends GcmTaskService {
    public static final String SIXTY_MINUTES = "360000";
    public static final String TASK_TAG_PERIODIC = "TASK_TAG_PERIODIC";

    @Override
    public int onRunTask(TaskParams taskParams) {
        getBaseContext().startService(new Intent(getBaseContext(), FetcherService.class).setAction(FetcherService.ACTION_REFRESH_FEEDS).putExtra(Constants.FROM_AUTO_REFRESH, true));

        return GcmNetworkManager.RESULT_SUCCESS;
    }

    public static void initAutoRefresh(Context context) {
        GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(context);

        long time = 3600L;
        try {
            time = Math.max(60L, Long.parseLong(PrefUtils.getString(PrefUtils.REFRESH_INTERVAL, SIXTY_MINUTES)) / 1000);
        } catch (Exception ignored) {
        }

        if (PrefUtils.getBoolean(PrefUtils.REFRESH_ENABLED, true)) {
            PeriodicTask task = new PeriodicTask.Builder()
                    .setService(AutoRefreshService.class)
                    .setTag(TASK_TAG_PERIODIC)
                    .setPeriod(time)
                    .setPersisted(true)
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .setUpdateCurrent(true)
                    .build();

            gcmNetworkManager.schedule(task);
        } else {
            gcmNetworkManager.cancelTask(TASK_TAG_PERIODIC, AutoRefreshService.class);
        }
    }

    public void addTopics() {
        String url = "Testign Waters";
        String topic = "httlp";
        FeedDataContentProvider.addFeed(this, url, topic, true);


    }
}
