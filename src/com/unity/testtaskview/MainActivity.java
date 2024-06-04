package com.unity.testtaskview;


import androidx.fragment.app.FragmentActivity;
import static android.app.ActivityTaskManager.INVALID_TASK_ID;
import static android.view.WindowManager.LayoutParams.PRIVATE_FLAG_TRUSTED_OVERLAY;

import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.android.wm.shell.TaskView;
import com.android.wm.shell.common.HandlerExecutor;
import android.widget.LinearLayout;


import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.MotionEvent;


public class MainActivity extends FragmentActivity implements IDisplayTouchListener{

    private static final boolean DEBUG = true;

    private static final String TAG = "MainActivity";
    private static final int INVALID_TASK_ID = -1;

    TaskViewManager mTaskViewManager;
    int mTaskViewTaskId  = INVALID_TASK_ID;
    TaskView mTaskView;
    boolean mTaskViewReady = false;
    private boolean mIsResumed;
    

    LinearLayout mGroup;

    private final TaskView.Listener mTaskViewListener = new TaskView.Listener() {
        public void onTaskVisibilityChanged(int taskId, boolean visible) {
        }
        public void onBackPressedOnTaskRoot(int taskId) {
        }

        public void onInitialized() {
            if (DEBUG) Log.d(TAG, "onInitialized()");
            mTaskViewReady = true;
            startIntentInTaskView();
        }
        public void onReleased() {
            if (DEBUG) Log.d(TAG, "onReleased(" +  ")");
            mTaskViewReady = false;
        }
        public void onTaskCreated(int taskId, ComponentName name) {

            if (DEBUG) Log.d(TAG, "onTaskCreated: taskId=" + taskId);
            mTaskViewTaskId = taskId;
        }
        public void onTaskRemovalStarted(int taskId) {
            if (DEBUG) Log.d(TAG, "onTaskRemovalStarted: taskId=" + taskId);
            mTaskViewTaskId = INVALID_TASK_ID;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Setting as trusted overlay to let touches pass through.
        getWindow().addPrivateFlags(PRIVATE_FLAG_TRUSTED_OVERLAY);
        // To pass touches to the underneath task.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        mGroup = (LinearLayout)findViewById(R.id.task_group);

        //mGroup.setVisibility(View.GONE);

        setUpTaskView(mGroup);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TaskInputManager inputManager = new TaskInputManager();
                inputManager.registerInputManager(MainActivity.this);

                // if((count++)%2==0){
                //     startIntentInTaskView();
                // }else{
                //     Log.e(TAG, "startActivity pause");
                //     //mTaskView.getHolder().removeCallback(mTaskView);
                //     //mTaskView.pauseTask();
                //     //ActivityInterceptor.performStop(MainActivity.this,false);
                //     //getWindow().getDecorView().getViewRootImpl().pauseWindow();
                //     //Intent intent = new Intent();
                //     //intent.setComponent(new ComponentName("com.unity.testtaskview","com.unity.testtaskview.ThirdActivity"));
                //     //startActivity(intent);
                //     //mTaskView.setEnabled(false);
                //     Intent intent = new Intent();
                //     intent.setAction("com.qiyi.video");
                //     intent.putExtra("self_action","performStop");
                //     sendBroadcast(intent);
                // }
            }
        });
        //getWindow().addPrivateFlags(PRIVATE_FLAG_TRUSTED_OVERLAY);

    }

    private void setUpTaskView(LinearLayout parent) {
        //把TaskViewManager进行构造
        mTaskViewManager = new TaskViewManager(this,
                new HandlerExecutor(new Handler(getMainLooper())));
        //创建对应的TaskView
        mTaskViewManager.createTaskView(taskView -> {
            taskView.setListener(getMainExecutor(), mTaskViewListener);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.width = 720;
            params.height = 1080;
            //把TaskView添加到了mapsCard
            parent.addView(taskView, params);
            
            Log.e(TAG, "activity add");
            mTaskView = taskView;
        });
    }


    private void startIntentInTaskView(){
        if (mTaskView == null || !mTaskViewReady) {
            return;
        }
        // If we happen to be be resurfaced into a multi display mode we skip launching content
        // in the activity view as we will get recreated anyway.
        if (isInMultiWindowMode() || isInPictureInPictureMode()) {
            return;
        }
        // Don't start Maps when the display is off for ActivityVisibilityTests.
        if (getDisplay().getState() != Display.STATE_ON) {
            return;
        }

        Log.e(TAG, "task activity found");

        try {
            if(mTaskViewTaskId != INVALID_TASK_ID){
                //mTaskViewManager.removeTask(mTaskViewTaskId);
            }
            //mTaskViewManager.removeTask(mTaskViewTaskId);
            ActivityOptions options = ActivityOptions.makeCustomAnimation(this,
                    /* enterResId= */ 0, /* exitResId= */ 0);
            // To show the Activity in TaskView, the Activity should be above the host task in
            // ActivityStack. This option only effects the host Activity is in resumed.
            options.setTaskAlwaysOnTop(true);
            
            mTaskView.startActivity(
                    PendingIntent.getActivity(this, /* requestCode= */ 0, getTaskIntent(),
                            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT),
                    /* fillInIntent= */ null, options, null /* launchBounds */);
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "Maps activity not found", e);
        }
    }

    private int count = 0;

    public Intent getTaskIntent(){
        if((count++)%2==0){
            Log.e(TAG, "com.qiyi.video start");
            return getIntent(this,"com.qiyi.video");
        }else{
            //return getIntent(this,"com.qiyi.video");
            //return getIntent(this,"com.tencent.wecarflow");
            Log.e(TAG, "com.unity.testtaskview start");
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.unity.testtaskview","com.unity.testtaskview.ThirdActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
            //return getIntent(this,"com.unity.map");
        }

    }


    public static Intent getIntent(Context context, String packageName){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        Log.e(TAG,"intent="+intent);
        return intent;
    }


    @Override
    protected void onPause() {
        super.onPause();
        mIsResumed = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTaskView != null && mTaskViewReady) {
            mTaskView.release();
            mTaskView = null;
        }
    }


    @Override
    public void onTouch(MotionEvent event){
        Log.e(TAG, "onTouch");
    }


}