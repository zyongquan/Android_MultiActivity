# 实验三 多Activity（周勇权 2024180750）

Android 移动应用开发 实验三：1 个 `MainActivity` + 4 个二级 `Activity`，分别演示
**Intent 正向传参**、**返回数据**、**隐式 Intent 调用系统相机**、**秒表（横竖屏连续计时）**。

- 开发语言：Java
- 编译工具链：AGP 9.3.3 + Gradle 9.5.0 + JDK 17（构建），compileSdk 37 / minSdk 24 / targetSdk 37
- 依赖：`androidx.appcompat`、`androidx.core`（FileProvider）
- 视图绑定：`viewBinding = true`（代码里不写 `findViewById`）
- 包名：`com.example.zhouyongquan2024180750`

## 一、作业要求对照表

| 作业要求 | 实现位置 | 说明 |
| --- | --- | --- |
| APP 标题为“姓名+学号” | `res/values/strings.xml` 的 `app_name`，`AndroidManifest.xml` 的 `android:label` | 每个页面标题栏都显示“周勇权2024180750”，副标题显示当前任务 |
| 任务1 正向传参 | `MainActivity.openForwardParamActivity()` → `ForwardParamActivity` | 显式 Intent + `putExtra()` 传 String/int/boolean，二级页面 `getIntent()` 取出显示 |
| 任务2 返回数据 | `ReturnDataActivity.confirmAndReturn()` → `MainActivity.onActivityResult()` | `startActivityForResult()` + `setResult(RESULT_OK, data)` + `finish()` |
| 任务3 隐式 Intent 拍照 | `CameraActivity` | `MediaStore.ACTION_IMAGE_CAPTURE` + `FileProvider` 原图输出 + 运行时权限 |
| 任务4 秒表横竖屏连续 | `StopWatchActivity` | `SystemClock.elapsedRealtime()` 基准 + `onSaveInstanceState()` 保存/恢复 |
| 禁止直接写字符串 | 所有 `res/layout/*.xml`、所有 Java 代码 | 全部使用 `@string/xxx` 或 `getString(R.string.xxx)` |
| 掌握 Activity 生命周期 | `BaseActivity` | 统一打印 `onCreate/onStart/onResume/onPause/onStop/onDestroy/onRestart/onSaveInstanceState` 日志 |

## 二、项目结构

```
app/src/main/java/com/example/zhouyongquan2024180750/
├── BaseActivity.java          所有页面的父类：生命周期日志 + ActionBar 副标题
├── ExtraKeys.java             Intent 传参用的 key 常量
├── MainActivity.java          主界面：4 个任务入口 + 任务2 返回值显示
├── ForwardParamActivity.java  任务1：接收并显示正向传递的参数
├── ReturnDataActivity.java    任务2：输入内容后 setResult 回传
├── CameraActivity.java        任务3：隐式 Intent 调系统相机、FileProvider 存原图、降采样显示
└── StopWatchActivity.java     任务4：秒表，横竖屏切换计时不中断

app/src/main/res/
├── layout/activity_main.xml、activity_forward_param.xml、activity_return_data.xml、
│         activity_camera.xml、activity_stop_watch.xml
├── values/strings.xml         全部界面文案（含排版占位的格式化字符串）
├── values/colors.xml、themes.xml
├── drawable/bg_button.xml、bg_card.xml、bg_edit.xml
└── xml/file_paths.xml         FileProvider 的路径白名单
```

## 三、如何运行

1. 用 Android Studio 打开本目录（`Open` → 选择 `MultiActivity` 文件夹），等 Gradle 同步完成。
2. 连接真机或启动模拟器，点击 ▶ 运行 `app`。
3. 命令行构建：`./gradlew assembleDebug`，APK 输出在 `app/build/outputs/apk/debug/`。

## 四、四个任务的关键点（答辩时会被问）

### 任务1 正向传参
- 显式 Intent 指定目标组件：`new Intent(this, ForwardParamActivity.class)`。
- `putExtra(key, value)` 放数据，二级页面用 **相同的 key 和类型** 取值（`getStringExtra` / `getIntExtra` / `getBooleanExtra`），类型不一致会取不到。
- key 集中放在 `ExtraKeys` 里，避免两边拼错。

### 任务2 返回数据
- `startActivityForResult(intent, REQUEST_CODE)` 启动；请求码用于区分是哪个请求返回的。
- 二级页面：`setResult(RESULT_OK, data)` 后 `finish()`；数据放在一个空的 `Intent` 里。
- 主页面在 `onActivityResult()` 中先判断 `requestCode`，再判断 `resultCode == RESULT_OK`。
- 若用户直接按返回键，`resultCode` 为 `RESULT_CANCELED`，界面给出“未返回数据”的提示。

### 任务3 隐式 Intent 拍照
1. `AndroidManifest.xml` 声明 `android.permission.CAMERA`；运行时用 `requestPermissions()` 动态申请（6.0+ 必须动态申请）。
2. 用 `MediaStore.ACTION_IMAGE_CAPTURE` 构造**隐式** Intent（只给动作，由系统挑选相机应用）。
3. 原图输出：先在 `getExternalFilesDir(Pictures)` 建文件，再用 `FileProvider.getUriForFile()` 得到
   `content://` Uri，通过 `MediaStore.EXTRA_OUTPUT` 交给相机，并加
   `FLAG_GRANT_WRITE_URI_PERMISSION`。直接传 `file://` 会在 Android 7+ 抛 `FileUriExposedException`。
4. Android 11+ 有包可见性限制，需要 `<queries>` 声明 `IMAGE_CAPTURE`，否则 `resolveActivity()` 返回 null。
5. `onActivityResult()` 中按 `inSampleSize` 两遍解码，避免大图 OOM；再按 EXIF 方向旋转。
6. 横竖屏切换时把照片路径存进 `onSaveInstanceState()`，重建后重新加载。

### 任务4 秒表横竖屏连续计时
- 显示值 = `elapsedMs + (SystemClock.elapsedRealtime() - anchorMs)`，刷新频率不影响精度；
  用 `elapsedRealtime()` 而非 `currentTimeMillis()`，避免改系统时间导致计时错乱。
- 旋转时 Activity 会销毁重建：`onSaveInstanceState()` 保存 `elapsedMs`、`anchorMs`、`running`，
  `onCreate()` 恢复。因为保留了**同一个起点 anchor**，旋转过程中流逝的时间也被算进去，所以计时连续。
- `onPause()` 只停掉 `Handler` 刷新（省电），不清数据；`onResume()` 重新开始刷新。

### 生命周期
`BaseActivity` 在每个回调里打一条日志，Logcat 里过滤 `tag:StopWatchActivity` 就能看到旋转时
`onPause → onStop → onSaveInstanceState → onDestroy → onCreate → onStart → onResume` 的完整顺序。

## 五、运行截图

见 `screenshots/` 目录：

| 文件 | 内容 |
| --- | --- |
| `01_main.png` | 主界面 4 个任务按钮 + 返回结果显示区，标题为“周勇权2024180750” |
| `02_forward_param.png` | 任务1 显示接收到的姓名/学号/年龄/是否在校 |
| `03_return_input.png` | 任务2 输入要返回的内容 |
| `04_main_result.png` | 任务2 返回后主界面显示结果 |
| `05_camera_page.png` | 任务3 页面：拍照按钮 + 照片预览区（隐式 Intent） |
| `06_camera_permission.png` | 任务3 首次点击“拍照”弹出的相机权限申请（标题即应用名） |
| `07_stopwatch_portrait.png` | 任务4 计时中（竖屏 00:03.3） |
| `08_stopwatch_landscape.png` | 任务4 旋转到横屏后仍连续计时（00:08.3，未清零） |
| `09_stopwatch_paused.png` | 任务4 暂停后停在 00:20.6 |

> 说明：任务3 的“拍照并显示照片”这一步在本机的 Android 模拟器上无法完成——模拟器的虚拟相机
> （`EmulatedCamera3_QemuSensor`）在按下快门后会卡住，连系统相机应用单独使用也卡住，
> 属于模拟器相机 HAL 的问题，与本项目代码无关。请用真机运行补一张“照片已显示”的截图。

## 六、提交到 GitHub

仓库已初始化并提交过，remote 也已配好，直接推送即可：

```bash
cd D:\Android\MultiActivity
git push -u origin master
```

远程地址是 `https://github.com/zyongquan/Android_MultiActivity.git`。
`local.properties`（本机 SDK 路径）和 `build/` 已在 `.gitignore` 中排除，不会提交。

## 七、实验报告

`实验三_多Activity-周勇权2024180750.docx` 已按学院模板填好，共 13 页包含：

- 封面：姓名周勇权、学号2024180750、GitHub 网址
- 实验环境与工程结构
- 主要代码及说明（清单文件、4 个任务的关键代码、生命周期日志）
- 作品运行截图 9 张（图1~图9，均为本机模拟器实际运行截图）
- 实验结果的分析与评价（4 个任务的结论、规范性检查、遇到的问题、不足与改进）

> 说明：任务3「拍照成功后显示照片」的界面截图没有收录。本机模拟器的虚拟相机按下快门后会卡死
> （系统相机应用单独打开拍照也一样卡住），属于模拟器相机 HAL 问题，与本项目代码无关；
> 报告中已如实说明这一点，该步骤可在真机上运行验证。

## 八、检查清单（提交前自查）

- [x] 5 个 Activity 都已注册，标题为「周勇权2024180750」
- [x] 任务1 正向传参（String/int/boolean 三种类型）
- [x] 任务2 `startActivityForResult` + `setResult` 返回数据，取消分支也处理
- [x] 任务3 隐式 Intent `ACTION_IMAGE_CAPTURE` + FileProvider + 运行时权限 + 降采样显示
- [x] 任务4 秒表横竖屏连续计时（`elapsedRealtime` + `onSaveInstanceState`）
- [x] 无任何硬编码界面字符串（Lint 无 HardcodedText 警告）
- [x] 构建通过（`assembleDebug` BUILD SUCCESSFUL）
- [x] 报告已生成（13 页，图1~图9 均为本机运行截图）
- [ ] `git push` 到 GitHub
