package com.sssemil.ir;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class IRService extends IntentService {

    public static final String EXTRA_STATE = "com.sssemil.ir.extra.STATE";
    private static final String ACTION_START = "com.sssemil.ir.action.START";
    private static final String ACTION_STOP = "com.sssemil.ir.action.STOP";
    private static final String ACTION_RESTART = "com.sssemil.ir.action.RESTART";
    private static final String ACTION_SEND = "com.sssemil.ir.action.SEND";
    private static final String ACTION_SEND_RAW = "com.sssemil.ir.action.SEND_RAW";
    private static final String ACTION_LEARN = "com.sssemil.ir.action.LEARN";
    private static final String EXTRA_FILENAME = "com.sssemil.ir.extra.FILENAME";
    private static final String EXTRA_KEY = "com.sssemil.ir.extra.KEY";
    private static final String EXTRA_LENGTH = "com.sssemil.ir.extra.LENGTH";

    public IRService() {
        super("IRService");
    }

    public static void setActionStart(Context context) {
        Intent intent = new Intent(context, IRService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }

    public static void setActionStop(Context context) {
        Intent intent = new Intent(context, IRService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    public static void setActionRestart(Context context) {
        Intent intent = new Intent(context, IRService.class);
        intent.setAction(ACTION_RESTART);
        context.startService(intent);
    }

    public static void setActionSend(Context context, String filename) {
        Intent intent = new Intent(context, IRService.class);
        intent.setAction(ACTION_SEND);
        intent.putExtra(EXTRA_FILENAME, filename);
        context.startService(intent);
    }

    public static void setActionSendRaw(Context context, String key, int lenght) {
        Intent intent = new Intent(context, IRService.class);
        intent.setAction(ACTION_SEND_RAW);
        intent.putExtra(EXTRA_KEY, key);
        intent.putExtra(EXTRA_LENGTH, lenght);
        context.startService(intent);
    }

    public static void setActionLearn(Context context, String filename) {
        Intent intent = new Intent(context, IRService.class);
        intent.setAction(ACTION_LEARN);
        intent.putExtra(EXTRA_FILENAME, filename);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            int state = 0;
            switch (action) {
                case ACTION_START:
                    state = handleActionStart();
                    break;
                case ACTION_STOP:
                    state = handleActionStop();
                    break;
                case ACTION_RESTART:
                    state = handleActionRestart();
                    break;
                case ACTION_SEND:
                    state = handleActionSend(intent.getStringExtra(EXTRA_FILENAME));
                    break;
                case ACTION_SEND_RAW:
                    final String key = intent.getStringExtra(EXTRA_KEY);
                    final int length = Integer.parseInt(intent.getStringExtra(EXTRA_LENGTH));
                    state = handleActionSendRaw(key, length);
                    break;
                case ACTION_LEARN:
                    state = handleActionLearn(intent.getStringExtra(EXTRA_FILENAME));
                    break;
            }

            /*Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(IRMain.ResponseReceiver.ACTION_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(EXTRA_STATE, state);
            sendBroadcast(broadcastIntent);*/
            //TODO: send state
        }
    }

    private int handleActionStart() {
        return IRCommon.start(this.getResources());
    }

    private int handleActionStop() {
        return IRCommon.stop(this.getResources());
    }

    private int handleActionRestart() {
        return IRCommon.restart(this.getResources());
    }

    private int handleActionSend(String filename) {
        return IRCommon.send(filename);
    }

    private int handleActionSendRaw(String key, int length) {
        return IRCommon.sendRaw(key, length);
    }

    private int handleActionLearn(String filename) {
        return IRCommon.learn(filename);
    }
}
