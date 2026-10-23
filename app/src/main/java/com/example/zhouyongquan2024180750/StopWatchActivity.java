package com.example.zhouyongquan2024180750;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.example.zhouyongquan2024180750.databinding.ActivityStopWatchBinding;

/**
 * 任务4：秒表（StopWatch）。
 *
 * <p>计时不靠“每 100ms 加 100ms”这种累加方式，而是记录两个量：</p>
 * <ul>
 *     <li>elapsedMs：本次计时“已经累计”的毫秒数；</li>
 *     <li>anchorMs：当前这一段计时的起点（SystemClock.elapsedRealtime()）。</li>
 * </ul>
 * <p>运行中的显示值 = elapsedMs + (现在 - anchorMs)，因此界面刷新频率
 * 不会影响计时精度。用 elapsedRealtime() 而不是 System.currentTimeMillis()，
 * 可以避免用户修改系统时间导致计时错乱。</p>
 *
 * <p>旋转屏幕时 Activity 会被销毁重建。只要把 elapsedMs、anchorMs、running
 * 三个值保存到 onSaveInstanceState()，重建后再用同一个 anchor 继续算，
 * 中间旋转消耗的时间也会被算进去，于是横屏竖屏切换后计时连续、不会清零。</p>
 */
public class StopWatchActivity extends BaseActivity {

    /** 界面刷新间隔（毫秒） */
    private static final long UPDATE_INTERVAL_MS = 50L;

    private static final String STATE_ELAPSED_MS = "state_elapsed_ms";
    private static final String STATE_ANCHOR_MS = "state_anchor_ms";
    private static final String STATE_RUNNING = "state_running";

    private ActivityStopWatchBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());

    /** 已经累计的毫秒数（不含正在进行的这一段） */
    private long elapsedMs = 0L;
    /** 正在进行的这一段的起点，未运行时为 0 */
    private long anchorMs = 0L;
    /** 是否正在计时 */
    private boolean running = false;

    /** 定时刷新界面的任务 */
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            refreshTimeText();
            if (running) {
                handler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStopWatchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBarSubtitle(R.string.subtitle_stopwatch);

        // 横竖屏切换重建时恢复计时状态
        if (savedInstanceState != null) {
            elapsedMs = savedInstanceState.getLong(STATE_ELAPSED_MS, 0L);
            anchorMs = savedInstanceState.getLong(STATE_ANCHOR_MS, 0L);
            running = savedInstanceState.getBoolean(STATE_RUNNING, false);
        }

        binding.btnStart.setOnClickListener(v -> startTiming());
        binding.btnPause.setOnClickListener(v -> pauseTiming());
        binding.btnReset.setOnClickListener(v -> resetTiming());

        refreshTimeText();
        updateButtonState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 回到前台（或旋转重建完成）后继续刷新显示
        if (running) {
            startTicker();
        }
        refreshTimeText();
        updateButtonState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 页面不可见时停止刷新界面，但不清空 anchor，计时逻辑本身不会中断
        stopTicker();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 只保存“原始累计值 + 本段起点 + 运行状态”，
        // 重建后继续用同一个起点计算，计时的连续性因此得到保证
        outState.putLong(STATE_ELAPSED_MS, elapsedMs);
        outState.putLong(STATE_ANCHOR_MS, anchorMs);
        outState.putBoolean(STATE_RUNNING, running);
    }

    @Override
    protected void onDestroy() {
        stopTicker();
        super.onDestroy();
    }

    /**
     * 开始计时：记下当前时刻作为这一段的起点。
     */
    private void startTiming() {
        if (running) {
            return;
        }
        running = true;
        anchorMs = SystemClock.elapsedRealtime();
        startTicker();
        refreshTimeText();
        updateButtonState();
    }

    /**
     * 暂停计时：把这一段的时长并入 elapsedMs。
     */
    private void pauseTiming() {
        if (!running) {
            return;
        }
        elapsedMs += SystemClock.elapsedRealtime() - anchorMs;
        running = false;
        anchorMs = 0L;
        stopTicker();
        refreshTimeText();
        updateButtonState();
    }

    /**
     * 重置：清零并停止计时。
     */
    private void resetTiming() {
        running = false;
        elapsedMs = 0L;
        anchorMs = 0L;
        stopTicker();
        refreshTimeText();
        updateButtonState();
    }

    /**
     * 计算当前总计时长（毫秒）。
     */
    private long currentElapsedMs() {
        if (running) {
            return elapsedMs + (SystemClock.elapsedRealtime() - anchorMs);
        }
        return elapsedMs;
    }

    /**
     * 把当前计时长格式化成 mm:ss.S 并显示。
     */
    private void refreshTimeText() {
        long total = currentElapsedMs();
        int minutes = (int) (total / 60000L);
        int seconds = (int) ((total / 1000L) % 60L);
        int tenths = (int) ((total % 1000L) / 100L);
        binding.tvTime.setText(getString(R.string.stopwatch_time_format, minutes, seconds, tenths));
    }

    /**
     * 计时中“开始”不可用，非计时中“暂停”不可用。
     */
    private void updateButtonState() {
        binding.btnStart.setEnabled(!running);
        binding.btnPause.setEnabled(running);
    }

    private void startTicker() {
        stopTicker();
        handler.post(ticker);
    }

    private void stopTicker() {
        handler.removeCallbacks(ticker);
    }
}
