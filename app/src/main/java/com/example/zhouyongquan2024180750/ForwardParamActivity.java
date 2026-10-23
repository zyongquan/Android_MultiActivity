package com.example.zhouyongquan2024180750;

import android.content.Intent;
import android.os.Bundle;

import com.example.zhouyongquan2024180750.databinding.ActivityForwardParamBinding;

/**
 * 任务1：接收并显示 MainActivity 正向传递过来的参数。
 *
 * <p>取值统一放在 onCreate() 里：用 getIntent() 拿到启动本页面的 Intent，
 * 再用与 putExtra() 相同的 key 和类型把值取出来。</p>
 */
public class ForwardParamActivity extends BaseActivity {

    private ActivityForwardParamBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForwardParamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBarSubtitle(R.string.subtitle_forward);

        Intent intent = getIntent();
        String name = intent.getStringExtra(ExtraKeys.EXTRA_NAME);
        String studentId = intent.getStringExtra(ExtraKeys.EXTRA_STUDENT_ID);
        int age = intent.getIntExtra(ExtraKeys.EXTRA_AGE, -1);
        boolean inSchool = intent.getBooleanExtra(ExtraKeys.EXTRA_IN_SCHOOL, false);

        // 取不到参数时显示“未收到该参数”，避免出现 null
        binding.tvName.setText(name == null ? getString(R.string.forward_value_empty) : name);
        binding.tvStudentId.setText(studentId == null
                ? getString(R.string.forward_value_empty) : studentId);
        binding.tvAge.setText(age < 0
                ? getString(R.string.forward_value_empty) : getString(R.string.forward_age_format, age));
        binding.tvInSchool.setText(inSchool
                ? R.string.forward_in_school_yes : R.string.forward_in_school_no);

        binding.btnBack.setOnClickListener(v -> finish());
    }
}
