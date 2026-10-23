package com.example.zhouyongquan2024180750;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.zhouyongquan2024180750.databinding.ActivityCameraBinding;

import java.io.File;
import java.io.IOException;

/**
 * 任务3：用隐式 Intent 调用系统相机拍照，并把拍到的照片显示在 ImageView 中。
 *
 * <p>整体流程：</p>
 * <ol>
 *     <li>检查并申请 CAMERA 权限；</li>
 *     <li>用 MediaStore.ACTION_IMAGE_CAPTURE 构造隐式 Intent（只给动作，不给具体组件，
 *         由系统挑选可用的相机应用）；</li>
 *     <li>通过 EXTRA_OUTPUT 指定原图的保存位置，并用 FileProvider 把私有目录里的文件
 *         包装成 content:// Uri 交给相机应用，同时临时授予读写权限；</li>
 *     <li>在 onActivityResult() 中把照片解码（按需降采样，避免大图 OOM）后显示。</li>
 * </ol>
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends BaseActivity {

    /** 拍照请求码 */
    private static final int REQUEST_CAPTURE_IMAGE = 2001;
    /** 申请相机权限的请求码 */
    private static final int REQUEST_CAMERA_PERMISSION = 2002;
    /** 旋转屏幕重建时保存照片路径，避免照片丢失 */
    private static final String STATE_PHOTO_PATH = "state_photo_path";
    /** 部分相机只回传缩略图时，Bitmap 放在 extras 的这个 key 里 */
    private static final String EXTRA_THUMBNAIL_KEY = "data";

    /** 解码目标尺寸的上限（像素），超过就降采样 */
    private static final int MAX_DECODE_WIDTH = 1080;
    private static final int MAX_DECODE_HEIGHT = 1600;

    private ActivityCameraBinding binding;

    /** 照片保存的文件 */
    private File photoFile;
    /** 交给相机的 content:// Uri */
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBarSubtitle(R.string.subtitle_camera);

        // 横竖屏切换后恢复已经拍好的照片
        if (savedInstanceState != null) {
            String path = savedInstanceState.getString(STATE_PHOTO_PATH);
            if (path != null) {
                photoFile = new File(path);
                showPhotoFromFile(photoFile);
            }
        }

        binding.btnTakePhoto.setOnClickListener(v -> requestPermissionThenTakePhoto());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (photoFile != null) {
            outState.putString(STATE_PHOTO_PATH, photoFile.getAbsolutePath());
        }
    }

    /**
     * 先检查相机权限：已授权直接拍照，未授权则动态申请。
     */
    private void requestPermissionThenTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    /**
     * 权限申请结果回调：授权成功才继续拍照。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_CAMERA_PERMISSION) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent();
        } else {
            Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 构造隐式 Intent 并启动系统相机。
     */
    private void dispatchTakePictureIntent() {
        // 1. 先在应用私有目录里创建照片文件，再用 FileProvider 得到 content:// Uri
        try {
            photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", photoFile);
        } catch (IOException | IllegalArgumentException e) {
            Log.e(logTag, "create photo file failed", e);
            Toast.makeText(this, R.string.camera_create_file_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 隐式 Intent：只声明“我要拍照”这个动作，不指定由哪个应用来响应
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 指定原图的输出位置，并临时授权相机应用写这个 Uri
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // 3. 先确认真的有应用能响应这个隐式 Intent，避免崩溃
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, R.string.camera_no_app, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.e(logTag, "start camera failed", e);
            Toast.makeText(this, R.string.camera_no_app, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 在 getExternalFilesDir(Pictures) 下创建一个以当前时间命名的 jpg 文件。
     */
    private File createImageFile() throws IOException {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null) {
            dir = getCacheDir();
        }
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("cannot create directory: " + dir);
        }
        return new File(dir, "IMG_" + System.currentTimeMillis() + ".jpg");
    }

    /**
     * 拍照返回：RESULT_OK 时把照片读出来显示。
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CAPTURE_IMAGE) {
            return;
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, R.string.camera_cancelled, Toast.LENGTH_SHORT).show();
            return;
        }

        // 指定了 EXTRA_OUTPUT 时，原图写在 photoFile 里；没写成功则尝试用缩略图兜底
        if (photoFile != null && photoFile.exists() && photoFile.length() > 0) {
            showPhotoFromFile(photoFile);
        } else if (data != null && data.getExtras() != null
                && data.getExtras().get(EXTRA_THUMBNAIL_KEY) instanceof Bitmap) {
            binding.ivPhoto.setImageBitmap(
                    (Bitmap) data.getExtras().get(EXTRA_THUMBNAIL_KEY));
        } else {
            Toast.makeText(this, R.string.camera_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 读取照片文件、按需降采样并按 EXIF 方向校正后显示。
     */
    private void showPhotoFromFile(File file) {
        if (file == null || !file.exists() || file.length() == 0) {
            return;
        }
        Bitmap bitmap = decodeSampledBitmap(file, MAX_DECODE_WIDTH, MAX_DECODE_HEIGHT);
        if (bitmap == null) {
            Toast.makeText(this, R.string.camera_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        binding.ivPhoto.setImageBitmap(applyExifRotation(file, bitmap));
    }

    /**
     * 两遍解码：先只读图片尺寸算出采样率，再按采样率真正解码，避免大图占用过多内存。
     */
    private Bitmap decodeSampledBitmap(File file, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    /**
     * 根据原图尺寸与目标尺寸计算 2 的幂次采样率。
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * 按 EXIF 里的旋转信息把照片转正，否则竖着拍的照片可能显示成横的。
     */
    private Bitmap applyExifRotation(File file, Bitmap bitmap) {
        int degrees;
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degrees = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degrees = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degrees = 270;
                    break;
                default:
                    return bitmap;
            }
        } catch (IOException | RuntimeException e) {
            Log.w(logTag, "read exif failed", e);
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
