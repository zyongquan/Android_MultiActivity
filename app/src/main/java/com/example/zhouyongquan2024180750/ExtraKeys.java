package com.example.zhouyongquan2024180750;

/**
 * Activity 之间传递数据所用的 key 常量。
 *
 * <p>把 key 集中写在一个类里，可以避免 MainActivity 与各个二级 Activity
 * 里手写字符串常量时出现的拼写不一致问题。</p>
 */
public final class ExtraKeys {

    /** 任务1：MainActivity → ForwardParamActivity，姓名（String） */
    public static final String EXTRA_NAME = "extra_name";

    /** 任务1：MainActivity → ForwardParamActivity，学号（String） */
    public static final String EXTRA_STUDENT_ID = "extra_student_id";

    /** 任务1：MainActivity → ForwardParamActivity，年龄（int） */
    public static final String EXTRA_AGE = "extra_age";

    /** 任务1：MainActivity → ForwardParamActivity，是否在校（boolean） */
    public static final String EXTRA_IN_SCHOOL = "extra_in_school";

    /** 任务2：ReturnDataActivity → MainActivity，回传的文本（String） */
    public static final String EXTRA_RETURN_TEXT = "extra_return_text";

    private ExtraKeys() {
        // 工具类，禁止实例化
    }
}
