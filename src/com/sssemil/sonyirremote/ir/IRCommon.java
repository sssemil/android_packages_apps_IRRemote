package com.sssemil.sonyirremote.ir;

public class IRCommon {
    static {
        System.loadLibrary("jni_sonyopenir");
    }

    private native int startIR();

    private native int stopIR();

    private native int learnKey(String filename);

    private native int sendKey(String filename);

    private native int sendRawKey(String key, int length);

    public int start() {
        return startIR();
    }

    public int stop() {
        return stopIR();
    }

    public int send(String filename) {
        return sendKey(filename);
    }

    public int sendRaw(String key, int length) {
        return sendRawKey(key, length);
    }

    public int learn(String filename) {
        return learnKey(filename);
    }

    public void restart() {
        stopIR();
        startIR();
    }

    protected IRCommon() { }

    private static IRCommon instance = null;

    public static IRCommon getInstance() {
        if (instance == null) {
            instance = new IRCommon();
        }
        return instance;
    }
}
