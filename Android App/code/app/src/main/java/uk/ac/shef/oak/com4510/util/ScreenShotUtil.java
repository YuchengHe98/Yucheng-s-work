package uk.ac.shef.oak.com4510.util;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import androidx.fragment.app.FragmentActivity;

public class ScreenShotUtil {
    private static final String TAG = "ScreenShotUtil";

    /**
     * 屏幕截图
     *
     * @param activity
     * @return
     */
    public static Bitmap screenShot(FragmentActivity activity, String filePath) {
        if (activity == null) {
            Log.e(TAG, "screenShot--->activity is null");
            return null;
        }
        View view = activity.getWindow().getDecorView();
        //允许当前窗口保存缓存信息
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        int navigationBarHeight = ScreenUtils.getStatusHeight(view.getContext());

        //获取屏幕宽和高
        int width = ScreenUtils.getScreenWidth(view.getContext());
        int height = ScreenUtils.getScreenHeight(view.getContext());

        // 全屏不用考虑状态栏，有导航栏需要加上导航栏高度
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, width,
                    height + navigationBarHeight);
        } catch (Exception e) {
            String msg = e.getMessage();
            // 部分手机导航栏高度不占窗口高度，不用添加，比如OppoR15这种异形屏
            if (msg.contains("<= bitmap.height()")) {
                try {
                    bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, width,
                            height);
                } catch (Exception e1) {
                    msg = e1.getMessage();
                    // 适配Vivo X21异形屏，状态栏和导航栏都没有填充
                    if (msg.contains("<= bitmap.height()")) {
                        try {
                            bitmap = Bitmap.createBitmap(view.getDrawingCache(), 0, 0, width,
                                    height - ScreenUtils.getStatusHeight(view.getContext()));
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    } else {
                        e1.printStackTrace();
                    }
                }
            } else {
                e.printStackTrace();
            }
        }
        //销毁缓存信息
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);

        if (null != bitmap) {
            try {
                compressAndGenImage(bitmap, filePath);
                Log.d(TAG, "--->截图保存地址：" + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static void compressAndGenImage(Bitmap image, String outPath) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // scale
        int options = 70;
        // Store the bitmap into output stream(no compress)
        image.compress(Bitmap.CompressFormat.JPEG, options, os);

        // Generate compressed image file
        FileOutputStream fos = new FileOutputStream(outPath);
        fos.write(os.toByteArray());
        Log.d(TAG, "compressAndGenImage--->文件大小：" + os.size() + "，压缩比例：" + options);
        fos.flush();
        fos.close();
    }
}
