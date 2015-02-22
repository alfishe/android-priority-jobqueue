package co.alfabits.android.jobqueue.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Sample implementation for NetworkUtil implementing internet accessibility via pinging Google's DNS server
 */
public class PingNetworkUtil implements NetworkUtil, NetworkEventProvider {
    //region Constants
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    //endregion

    //region Fields
    private NetworkEventProvider.Listener listener;
    private boolean isConnected;
    //endregion

    //region Constructors
    public PingNetworkUtil(Context context) {
        context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkConnection();
            }
        }, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        checkConnection();
    }
    //endregion

    @Override
    public boolean isConnected(Context context) {
        return isConnected;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void notifyListener() {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null)
                    listener.onNetworkChange(isConnected);
            }
        });
    }

    private void checkConnection() {
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                isConnected = ping();
                notifyListener();
            }
        });
    }

    private boolean ping() {
        boolean result = false;

        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            result = (exitValue == 0);
        }
        catch (IOException | InterruptedException ignored) {
        }

        return result;
    }
}