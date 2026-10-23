package com.example.zhouyongquan2024180750;

import android.content.Intent;
import android.os.Bundle;

import com.example.zhouyongquan2024180750.databinding.ActivityMainBinding;

/**
 * 主界面：4 个按钮分别启动 4 个二级 Activity，并显示任务2 回传的数据。
 *
 * <ul>
 *     <li>任务1：显式 Intent + putExtra() 正向传参</li>
 *     <li>任务2：startActivityForResult() 启动，onActivityResult() 接收返回值</li>
 *     <li>任务3：跳转到相机拍照页面</li>
 *     <li>任务4：跳转到秒表页面</li>
 * </ul>
 */
@SuppressWarnings("deprecation")
public class MainActivity extends BaseActivity {

    /** 任务2 的请求码，用于在 onActivityResult() 中区分是哪个请求返回的 */
    private static final int REQUEST_CODE_RETURN_DATA = 1001;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBarSubtitle(R.string.subtitle_main);

        // 任务1：显式 Intent 正向传参
        binding.btnTask1.setOnClickListener(v -> openForwardParamActivity());

        // 任务2：带结果返回的启动方式
        binding.btnTask2.setOnClickListener(v -> openReturnDataActivity());

        // 任务3：隐式 Intent 调用系统相机（在 CameraActivity 中完成）
        binding.btnTask3.setOnClickListener(v -> startActivity(new Intent(this, CameraActivity.class)));

        // 任务4：秒表计时
        binding.btnTask4.setOnClickListener(v -> startActivity(new Intent(this, StopWatchActivity.class)));
    }

    /**
     * 任务1：用显式 Intent 指定目标 Activity，并通过 putExtra() 把参数传过去。
     * 这里放了 String、int、boolean 三种类型的参数。
     */
    private void openForwardParamActivity() {
        Intent intent = new Intent(MainActivity.this, ForwardParamActivity.class);
        intent.putExtra(ExtraKeys.EXTRA_NAME, getString(R.string.author_name));
        intent.putExtra(ExtraKeys.EXTRA_STUDENT_ID, getString(R.string.author_student_id));
        intent.putExtra(ExtraKeys.EXTRA_AGE, 20);
        intent.putExtra(ExtraKeys.EXTRA_IN_SCHOOL, true);
        startActivity(intent);
    }

    /**
     * 任务2：用 startActivityForResult() 启动，这样可以拿到二级 Activity 的返回值。
     *
     * @param requestCode 请求码，返回时用来区分来源
     */
    private void openReturnDataActivity() {
        Intent intent = new Intent(MainActivity.this, ReturnDataActivity.class);
        startActivityForResult(intent, REQUEST_CODE_RETURN_DATA);
    }

    /**
     * 任务2：接收 ReturnDataActivity 通过 setResult() 回传的数据。
     *
     * <p>只有 resultCode == RESULT_OK 才取数据；如果用户在任务2页面直接按返回键，
     * resultCode 会是 RESULT_CANCELED，说明没有回传内容。</p>
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE_RETURN_DATA) {
            return;
        }
        if (resultCode == RESULT_OK && data != null) {
            String text = data.getStringExtra(ExtraKeys.EXTRA_RETURN_TEXT);
            binding.tvResult.setText(getString(R.string.main_result_format,
                    text == null ? "" : text));
        } else {
            binding.tvResult.setText(R.string.main_result_cancelled);
        }
    }
}
