package com.xormedia.mylib;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;

public class MyThread {
    private ArrayList<WeakReference<Handler>> mWeakDrawHandlers = new ArrayList<WeakReference<Handler>>();
    private myRunable mRunable = null;
    private Message msg = null;
    private Thread mThread = null;

    public interface myRunable {
        public void run(Message msg);
    }

    public MyThread(myRunable runable) {
        if (runable != null) {
            mRunable = runable;
        }
    }

    public synchronized void start(Handler handler) {
        if (handler != null) {
            synchronized (mWeakDrawHandlers) {
                boolean find = false;
                for (int i = 0; i < mWeakDrawHandlers.size(); i++) {
                    if (mWeakDrawHandlers.get(i) != null && mWeakDrawHandlers.get(i).get() != null && mWeakDrawHandlers.get(i).get() == handler) {
                        find = true;
                        break;
                    }
                }
                if (find == false) {
                    mWeakDrawHandlers.add(new WeakReference<Handler>(handler));
                }
            }
        }
        if (mThread == null) {
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    msg = new Message();
                    msg.what = 0;
                    if (mRunable != null) {
                        mRunable.run(msg);
                    }
                    synchronized (mWeakDrawHandlers) {
                        for (int i = 0; i < mWeakDrawHandlers.size(); i++) {
                            if (mWeakDrawHandlers.get(i) != null && mWeakDrawHandlers.get(i).get() != null) {
                                mWeakDrawHandlers.get(i).get().sendMessage(msg);
                                mWeakDrawHandlers.set(i, null);
                            }
                        }
                        mWeakDrawHandlers.clear();
                    }
                    mThread = null;
                }
            });
            mThread.start();
        }
    }

    public void stop() {
        synchronized (mWeakDrawHandlers) {
            mWeakDrawHandlers.clear();
        }
    }
}
