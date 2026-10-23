package com.example.zhouyongquan2024180750;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.zhouyongquan2024180750.databinding.ActivityReturnDataBinding;

/**
 * 任务2：在输入框里输入内容，点击“确认返回”后把内容带回 MainActivity。
 *
 * <p>回传数据分两步：先用 putExtra() 把数据放进一个空的 Intent，
 * 再调用 setResult(RESULT_OK, intent) 设置返回结果，最后 finish() 关闭本页面。
 * MainActivity 会在 onActivityResult() 中收到这个 Intent。</p>
 */
public class ReturnDataActivity extends BaseActivity {

    private ActivityReturnDataBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReturnDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBarSubtitle(R.string.subtitle_return);

        binding.btnConfirmReturn.setOnClickListener(v -> confirmAndReturn());
    }

    /**
     * 校验输入内容，非空则带着结果关闭当前页面。
     */
    private void confirmAndReturn() {
        String text = binding.etInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, R.string.return_empty_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        // 回传数据用的 Intent 不需要指定目标组件，只承载数据
        Intent data = new Intent();
        data.putExtra(ExtraKeys.EXTRA_RETURN_TEXT, text);
        setResult(RESULT_OK, data);
        finish();
    }
}
