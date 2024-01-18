package com.cy.switchbuttonniubility;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description:
 * @Author: cy
 * @CreateDate: 2020/12/4 10:15
 * @UpdateUser:
 * @UpdateDate: 2020/12/4 10:15
 * @UpdateRemark:
 * @Version:
 */
public class ThreadUtils {
    private ExecutorService executorService;
    private ExecutorService executorServiceSingle;
    private static Handler handlerMain = new Handler(Looper.getMainLooper());

    private ThreadUtils() {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        executorServiceSingle = Executors.newSingleThreadExecutor();
    }

    private static class ThreadUtilsHolder {
        private static ThreadUtils instance = new ThreadUtils();
    }

    public static ThreadUtils getInstance() {
        return ThreadUtilsHolder.instance;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    private <T, L extends LifecycleOwner> void run_lifecycle(final L lifecycleOwner, final Dialog dialog, @NonNull final RunnableCallback<T> runnableCallback) {
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (lifecycleOwner != null)
                    lifecycleOwner.getLifecycle().addObserver(new LifecycleEventObserver() {
                        @Override
                        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                            if (event == Lifecycle.Event.ON_DESTROY) {
                                runnableCallback.interrupt();
                                runnableCallback.onDestroyed();
                                lifecycleOwner.getLifecycle().removeObserver(this);
                            }
                        }
                    });
                if (dialog != null)
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            runnableCallback.interrupt();
                        }
                    });
            }
        });
    }

    public <T> void runThread(final RunnableCallback<T> runnableCallback) {
        executorService.execute(runnableCallback);
    }

    public <T> void runThread(final Runnable runnable) {
        executorService.execute(runnable);
    }

    public <T, L extends LifecycleOwner> void runThread(final L lifecycleOwner, final Dialog dialog, @NonNull final RunnableCallback<T> runnableCallback) {
        run_lifecycle(lifecycleOwner, dialog, runnableCallback);
        runThread(runnableCallback);
    }

    public <T, L extends LifecycleOwner> void runThread(final L lifecycleOwner, @NonNull final RunnableCallback<T> runnableCallback) {
        runThread(lifecycleOwner, null, runnableCallback);
    }


    public <T> void runSingleThread(final RunnableCallback<T> runnableCallback) {
        executorServiceSingle.execute(runnableCallback);
    }

    public static <T> void runThread(ExecutorService executorService, final RunnableCallback<T> runnableCallback) {
        executorService.execute(runnableCallback);
    }

    public <T, L extends LifecycleOwner> void runSingleThread(final L lifecycleOwner, final Dialog dialog, @NonNull final RunnableCallback<T> runnableCallback) {
        run_lifecycle(lifecycleOwner, dialog, runnableCallback);
        runSingleThread(runnableCallback);
    }

    public <T, L extends LifecycleOwner> void runSingleThread(final L lifecycleOwner, @NonNull final RunnableCallback<T> runnableCallback) {
        runSingleThread(lifecycleOwner, null, runnableCallback);
    }


    public static void runOnUIThread(Runnable runnable) {
        handlerMain.post(runnable);
    }

    public abstract static class RunnableCallbackSimple<T> extends RunnableCallback<T> {
        @Override
        public void runUIThread(T result) {

        }
    }



    public abstract static class RunnableCallback<T> implements Runnable {
        private volatile boolean isInterrupted = false;

        public final boolean isInterrupted() {
            return isInterrupted;
        }

        public final void interrupt() {
            isInterrupted = true;
        }

        @Override
        public final void run() {
            final T t = runThread();
            runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    runUIThread(t);
                }
            });
        }

        public abstract T runThread();

        public abstract void runUIThread(T result);

        public void onDestroyed() {
        }
    }
    public static interface RunnableCallbackResult<T> {
        public void runUIThread(T result);
    }
}
