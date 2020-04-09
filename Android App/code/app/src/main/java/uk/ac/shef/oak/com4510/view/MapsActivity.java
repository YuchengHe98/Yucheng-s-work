package uk.ac.shef.oak.com4510.view;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import uk.ac.shef.oak.com4510.R;
import uk.ac.shef.oak.com4510.bean.ImageInfo;
import uk.ac.shef.oak.com4510.bean.PhotoInfo;
import uk.ac.shef.oak.com4510.dataBase.PhotoDAO;
import uk.ac.shef.oak.com4510.dataBase.PhotoRoomDatabase;
import uk.ac.shef.oak.com4510.util.BitmapUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.icu.math.BigDecimal;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int ACCESS_FINE_LOCATION = 123;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private MapView mapView;
    private Button mButtonEnd;
    private ImageButton cameraB;
    private String photoName;
    private String title;
    private String pathName;
    private String createTime;
    private String provider;
    private int photoNum; //记录该主题拍了多少张照片

    private LocationManager locationManager;

    private static final String TAG = "MapsActivity";

    //截屏参数
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private static Intent mResultData = null;
    private ImageReader mImageReader;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private GestureDetector mGestureDetector;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    public static final int REQUEST_MEDIA_PROJECTION = 18;

    private Map<String, Location> pathHashMap;
    private List<Location> locationList;
    private List<String> photoNameList;
    /**
     * 温度
     */
    private String temp;

    /**
     * 气压
     */
    private String pressure;

    private SensorManager sensorManager;

    private PhotoInsertTask photoInsertTask;
    private PhotoUpdateTask photoUpdateTask;

    public static final int TAKE_PHOTO = 1;
    public static final int TAKE_ALBUM = 2;

    private List<Marker> removeMarkers;

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //根据标记点，进行相关截图操作
            //把上一个标记点删了
            if (lastMarker != null) {
                lastMarker.remove();
            }
            String photoName = photoNameList.get(photoNameList.size() - photoNum);
            Location markLocation = pathHashMap.get(photoName);
            if (removeMarkers != null) {
                for (Marker marker : removeMarkers) {
                    marker.remove();
                }
            }
            removeMarkers = new ArrayList<>();
            for (Location location : locationList) {
                if (markLocation.equals(location)) {
                    //标红
                    Marker temp = mMap.addMarker(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                            .title(mLastUpdateTime));
                    removeMarkers.add(temp);
                } else {
                    //其他点标记灰色
                    removeMarkers.add(mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                            .title(mLastUpdateTime)));
                }
            }
            //获取屏幕截图，保存到本地
            File savePath = getExternalFilesDir("paths");
            pathName = savePath.getAbsolutePath() + "/path_" + System.currentTimeMillis() + ".jpg";
            //开始截屏操作
            startCapture(photoName, pathName);
        }
    };

    //传感器监听器
    private SensorEventListener listener = new SensorEventListener() {
        @Override//传感器改变时调用
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;//获取结果
            switch (event.sensor.getType()) {//获取触发的传感器类型
                case Sensor.TYPE_AMBIENT_TEMPERATURE://温度传感器
//                    Toast.makeText(getApplicationContext(), "当前温度为：" + values[0], Toast.LENGTH_LONG).show();
                    BigDecimal bigDecimal1 = new BigDecimal(values[0]);
                    temp = bigDecimal1.setScale(0, BigDecimal.ROUND_HALF_UP).intValue() + "";
                    break;
                case Sensor.TYPE_PRESSURE://压力传感器
//                    Toast.makeText(getApplicationContext(), "当前压力为：" + values[0], Toast.LENGTH_LONG).show();
                    BigDecimal bigDecimal = new BigDecimal(values[0]);
                    pressure = bigDecimal.setScale(0, BigDecimal.ROUND_HALF_UP).intValue() + "";
                    break;
                default:
                    break;
            }
        }

        @Override//传感器进度发生改变时调用
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pathHashMap = new HashMap<>();
        locationList = new ArrayList<>();
        photoNameList = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        title = bundle.getString("title");

        mButtonEnd = findViewById(R.id.visit_end);
        cameraB = findViewById(R.id.camera_b);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        createTime = df.format(new Date());

        cameraB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopwindow();
            }
        });

        mButtonEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this)
                        .setMessage("Save this screen?")
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                stopLocationUpdates();
                                //消息要先传进Message中，再由Message传递给Handler处理
                                Message msg = Message.obtain();
                                msg.arg1 = photoNum;
                                myHandler.sendMessage(msg);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //把该路线拍的照的deleteFlag都设为1
                                PhotoInfo pathInfo = new PhotoInfo();
                                pathInfo.setDeleteFlag(1);
                                photoUpdateTask = new PhotoUpdateTask();
                                photoUpdateTask.execute(pathInfo);
                                stopLocationUpdates();
                            }
                        }).create();
                alertDialog.show();
            }
        });

        //获取传感器管理者
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //为传感器管理者注册监听器(指明监听哪个传感器和监听速率)NORMAL为标准
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
                SensorManager.SENSOR_DELAY_UI);//温度传感器
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_UI);//压力传感器

        //检查相机权限
        checkPermissions(getApplicationContext());

        //截屏初始化参数
        mediaProjectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);
    }

    private void checkPermissions(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("Camera permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CAMERA}, 1);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
                }

            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("Writing external storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(listener);
        }
        try {
            stopVirtual();
            tearDownMediaProjection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示popupWindow
     */
    private void showPopwindow() {
        // 利用layoutInflater获得View
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.popup_window, null);

        // 下面是两种方法得到宽度和高度 getWindow().getDecorView().getWidth()

        PopupWindow window = new PopupWindow(view,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);

        // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
        window.setFocusable(true);

        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        window.setBackgroundDrawable(dw);


        // 设置popWindow的显示和消失动画
        window.setAnimationStyle(R.style.mypopwindow_anim_style);
        // 在底部显示
        window.showAtLocation(MapsActivity.this.findViewById(R.id.camera_b),
                Gravity.BOTTOM, 0, 0);
        // 这里检验popWindow里的button是否可以点击
        Button screen = view.findViewById(R.id.screen);
        screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建File对象，用于存储拍照后的图片
                photoName = "outputImage_" + System.currentTimeMillis() + ".jpg";
                File savepath = getExternalFilesDir("photos");
                if (!savepath.exists()) {
                    savepath.mkdir();
                }
                File outputImage = new File(savepath, photoName);
                photoName = outputImage.getAbsolutePath();
                Uri imageUri = null;
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(MapsActivity.this,
                            "uk.ac.shef.oak.com4510.fileprovider", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }
                //启动相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
                window.dismiss();
            }
        });
        Button uploadPhotos = view.findViewById(R.id.upload_photos);
        uploadPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //访问相册
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, TAKE_ALBUM);//打开相册
                window.dismiss();
            }
        });
        Button cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ACCESS_FINE_LOCATION);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
    }


    private class PhotoInsertTask extends AsyncTask<PhotoInfo, Void, Integer> {
        @Override
        protected Integer doInBackground(PhotoInfo... photoInfos) {
            PhotoDAO photoDAO = PhotoRoomDatabase.getDatabase(MapsActivity.this).photoDAO();
            PhotoInfo photoInfo = photoInfos[0];

            //查看该图片是否有保存了
            PhotoInfo temp = photoDAO.queryPhotosByPhotoFile(photoInfo.getPhotoFile());
            if (temp == null) {
                photoDAO.insert(photoInfo);
                return 1;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer type) {
            String content = "";
            if (type.equals(1)) {
                content = "upload success !!";
            } else {
                content = "the photo is exsited";
            }
            Toast toast = Toast.makeText(MapsActivity.this, content, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private class PhotoUpdateTask extends AsyncTask<PhotoInfo, Void, Integer> {
        @Override
        protected Integer doInBackground(PhotoInfo... photoInfos) {
            PhotoDAO photoDAO = PhotoRoomDatabase.getDatabase(MapsActivity.this).photoDAO();
            PhotoInfo pathInfo = photoInfos[0];
            if (pathInfo.getDeleteFlag() == 0) {
                PhotoInfo photoInfo = photoDAO.queryPhotosByPhotoFile(pathInfo.getPhotoFile());
                photoInfo.setPathFile(pathInfo.getPathFile());
                photoDAO.updateAllPhotos(photoInfo);
            } else if (pathInfo.getDeleteFlag() == 1) {
                List<PhotoInfo> photoInfoList = photoDAO.queryPhotosByTitle(pathInfo.getPhotoFile());
                for (PhotoInfo p : photoInfoList) {
                    p.setDeleteFlag(1);
                }
                photoDAO.updateAllPhotos(photoInfoList.toArray(new PhotoInfo[photoInfoList.size()]));
            }
            return pathInfo.getDeleteFlag();
        }

        @Override
        protected void onPostExecute(Integer deleteFlag) {
            String content;
            if (deleteFlag == 1) {
                content = "cancel save !!";
                Toast toast = Toast.makeText(MapsActivity.this, content, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                photoNum = photoNum - 1;
                if (photoNum == 0) {
                    content = "update success !!";
                    Toast toast = Toast.makeText(MapsActivity.this, content, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Message msg = Message.obtain();
                    msg.arg1 = photoNum;
                    myHandler.sendMessage(msg);
                }
            }

        }
    }

    /**
     * it stops the location updates
     */
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);  //设置位置请求间隔
        mLocationRequest.setFastestInterval(500); //设置最快请求间隔
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        startLocationUpdates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                photoNum++;
                photoNameList.add(photoName);
                int angle = BitmapUtil.readPictureDegree(photoName);
                Bitmap myBitmap = BitmapFactory.decodeFile(photoName);
                Bitmap newBitmap = BitmapUtil.rotaingImageView(angle, myBitmap);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(new File(photoName));
                    newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                pathHashMap.put(photoName, mCurrentLocation);
                locationList.add(mCurrentLocation);

                PhotoInfo photoInfo = new PhotoInfo();
                photoInfo.setTitle(title);
                photoInfo.setPhotoFile(photoName);
                photoInfo.setTemp(temp);
                photoInfo.setPressure(pressure);
                photoInfo.setCreateTime(createTime);
                photoInsertTask = new PhotoInsertTask();
                photoInsertTask.execute(photoInfo);
                break;
            case REQUEST_MEDIA_PROJECTION:
                mResultData = data;
                break;
            case TAKE_ALBUM:
                if (data == null) {
                    return;
                }
                String localPath = handleImageOnKitKat(data);
                //把相册的照片复制到app指定位置
                photoName = "outputImage_" + System.currentTimeMillis() + ".jpg";
                File savepath = getExternalFilesDir("photos");
                File outputImage = new File(savepath, photoName);
                photoName = outputImage.getAbsolutePath();
                // 创建一个带缓冲区的输入流
                BufferedInputStream bis = null;
                BufferedOutputStream bos = null;
                try {
                    bis = new BufferedInputStream(new FileInputStream(localPath));
                    bos = new BufferedOutputStream(new FileOutputStream(outputImage));
                    // 定义一个字节数组，作为缓冲区
                    byte[] buff = new byte[1024];
                    int len;
                    while ((len = bis.read(buff)) != -1) {
                        bos.write(buff, 0, len); // 从第一个字节开始，向文件写入len个字节
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bis.close();
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                pathHashMap.put(photoName, mCurrentLocation);
                locationList.add(mCurrentLocation);
                photoNum++;
                photoNameList.add(photoName);

                PhotoInfo photoInfo1 = new PhotoInfo();
                photoInfo1.setTitle(title);
                photoInfo1.setPhotoFile(photoName);
                photoInfo1.setTemp(temp);
                photoInfo1.setPressure(pressure);
                photoInfo1.setCreateTime(createTime);
                photoInsertTask = new PhotoInsertTask();
                photoInsertTask.execute(photoInfo1);
                break;
        }
    }

    private String handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(MapsActivity.this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);

            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        return imagePath;
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void startScreenShot(String photoName, String pathName) {
        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            public void run() {
                // start virtual
                startVirtual();
            }
        }, 5);

        handler1.postDelayed(new Runnable() {
            public void run() {
                // capture the screen
                startCapture(photoName, pathName);
            }
        }, 30);
    }

    public void startVirtual() {
        if (mMediaProjection != null) {
            virtualDisplay();
        } else {
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }

    private void startCapture(String photoName, String pathName) {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Image image = mImageReader.acquireLatestImage();

        if (image == null) {
            startScreenShot(photoName, pathName);
        } else {
            SaveTask mSaveTask = new SaveTask();
            ImageInfo imageInfo = new ImageInfo();
            imageInfo.setImage(image);
            imageInfo.setPhotoName(photoName);
            imageInfo.setPathName(pathName);
            // mSaveTask.execute(image);
            if (Build.VERSION.SDK_INT >= 11) {
                // From API 11 onwards, we need to manually select the
                // THREAD_POOL_EXECUTOR
                AsyncTaskCompatHoneycomb.executeParallel(mSaveTask, imageInfo);
            } else {
                // Before API 11, all tasks were run in parallel
                mSaveTask.execute(imageInfo);
            }
            // AsyncTaskCompat.executeParallel(mSaveTask, image);
        }
    }

    static class AsyncTaskCompatHoneycomb {
        static <Params, Progress, Result> void executeParallel(AsyncTask<Params, Progress, Result> task, Params... params) {
            // 这里显示调用了THREAD_POOL_EXECUTOR，所以就可以使用该线程池了
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }

    }

    @SuppressLint("NewApi")
    private void virtualDisplay() {
        Surface sf = mImageReader.getSurface();
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                "screen-mirror", mScreenWidth, mScreenHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    public void setUpMediaProjection() {
        if (mResultData == null) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(intent);
        } else {
            mMediaProjection = getMediaProjectionManager().getMediaProjection(
                    Activity.RESULT_OK, mResultData);
        }
    }

    private MediaProjectionManager getMediaProjectionManager() {
        return (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    public class SaveTask extends AsyncTask<ImageInfo, Void, ImageInfo> {

        @Override
        protected ImageInfo doInBackground(ImageInfo... params) {
            if (params == null || params.length < 1 || params[0] == null) {
                return null;
            }

            ImageInfo imageInfo = params[0];
            Image image = imageInfo.getImage();
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            // 每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            // 总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            image.close();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(new File(pathName));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            return imageInfo;
        }

        @Override
        protected void onPostExecute(ImageInfo imageInfo) {
            PhotoInfo pathInfo = new PhotoInfo();
            pathInfo.setPathFile(imageInfo.getPathName());
            pathInfo.setPhotoFile(imageInfo.getPhotoName());
            pathInfo.setDeleteFlag(0);
            photoUpdateTask = new PhotoUpdateTask();
            photoUpdateTask.execute(pathInfo);
        }
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }


    private Location mCurrentLocation; //当前位置
    private Location lastLocation;  //上一次的位置
    private Marker lastMarker; //上一次的标志
    private String mLastUpdateTime;
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            mCurrentLocation = locationResult.getLastLocation();
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            Log.i("MAP", "new location " + mCurrentLocation.toString());
            if (mMap != null) {
                //把上一个标记点删了
                if (lastMarker != null) {
                    lastMarker.remove();
                }
                lastMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                        .title(mLastUpdateTime));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 14.0f));

                //添加路线图
                if (lastLocation != null) {
                    mMap.addPolyline(new PolylineOptions().add(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                            new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())).width(25).color(Color.BLUE).geodesic(true));
                    lastLocation = mCurrentLocation;
                }
            }
        }
    };


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback, null /* Looper */);

                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    List<String> providerList = locationManager.getProviders(true);
                    if (providerList.contains(LocationManager.GPS_PROVIDER)) {
                        provider = LocationManager.GPS_PROVIDER;
                    } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
                        provider = LocationManager.NETWORK_PROVIDER;
                    } else {
                        Toast.makeText(this, "No location provider to use", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    lastLocation = locationManager.getLastKnownLocation(provider);
                    mCurrentLocation = lastLocation;
                    if (lastLocation != null) {
                        LatLng sydney = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                        lastMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker my location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(this, "No location provider to use", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        lastLocation = locationManager.getLastKnownLocation(provider);
        mCurrentLocation = lastLocation;
        if (lastLocation != null) {
            LatLng sydney = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            lastMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker my location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }

    }

}
