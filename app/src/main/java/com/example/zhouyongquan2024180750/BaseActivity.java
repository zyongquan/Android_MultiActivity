package com.example.zhouyongquan2024180750;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 所有页面的公共父类，统一打印 Activity 生命周期日志。
 *
 * <p>对应实验目的(3)“掌握 Activity 生命周期”：在 MainActivity 与 4 个二级 Activity
 * 之间跳转、按下返回键、旋转屏幕时，Logcat 中可以看到
 * onStart / onResume / onPause / onStop / onDestroy 的完整调用顺序，
 * 每个 Activity 的日志 tag 就是它自己的类名。</p>
 *
 * <p>ActionBar 的标题由 AndroidManifest.xml 中 application 的 android:label
 * 决定（即“姓名+学号”），这里只负责给每个页面设置说明当前任务的副标题。</p>
 */
public class BaseActivity extends AppCompatActivity {

    /** Logcat 中按类名过滤日志，例如 tag:CameraActivity */
    protected final String logTag = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(logTag, "onCreate()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(logTag, "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(logTag, "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(logTag, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(logTag, "onStop()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(logTag, "onRestart()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(logTag, "onDestroy()");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(logTag, "onSaveInstanceState()");
    }

    /**
     * 设置 ActionBar 副标题，用来说明当前页面属于哪个任务。
     *
     * @param subtitleResId 副标题字符串资源 id
     */
    protected void setBarSubtitle(@StringRes int subtitleResId) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(subtitleResId);
        }
    }
}
