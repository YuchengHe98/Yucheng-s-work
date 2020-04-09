package uk.ac.shef.oak.com4510.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import uk.ac.shef.oak.com4510.bean.LayoutTypeEnum;
import uk.ac.shef.oak.com4510.R;
import uk.ac.shef.oak.com4510.adapter.PhotoAdapter;
import uk.ac.shef.oak.com4510.bean.PathInfo;
import uk.ac.shef.oak.com4510.bean.PhotoInfo;
import uk.ac.shef.oak.com4510.dataBase.PhotoDAO;
import uk.ac.shef.oak.com4510.dataBase.PhotoRoomDatabase;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 首页
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2987;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 7829;
    private static final String TAG = "MainActivity";
    private PhotoAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Button visit;

    private Spinner sort;

    private Activity activity;

    private PhotoLoadTask photoLoadTask;

    private int numberOfColumns;

    //一次性把所有文件信息加载出来
    private List<PhotoInfo> allPhotoInfos = new ArrayList<>();
    private List<PhotoInfo> allPathPhotos = new ArrayList<>();
    private List<PhotoInfo> allPaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        numberOfColumns = 3;

        mRecyclerView = findViewById(R.id.grid_recycler_view);
        sort = findViewById(R.id.sort);
        visit = findViewById(R.id.visit);

        //初始化排序
        //数据
        List<String> data_list = new ArrayList<String>();
        data_list.add("ALL  photos");
        data_list.add("Path photos");
        data_list.add("Path list");

        //适配器
        ArrayAdapter<String> arr_adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data_list);
        //设置样式
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        sort.setAdapter(arr_adapter);

        sort.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {//选择item的选择点击监听事件
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                //根据不同的选择，选择对应的布局
                Log.i(TAG, "arg2:" + arg2 + "arge3:" + arg3);
                if (arg2 == LayoutTypeEnum.ALL_PHOTOS.getValue()) {
                    GridLayoutManager layoutManager = new GridLayoutManager(MainActivity.this, numberOfColumns);
                    mRecyclerView.setLayoutManager(layoutManager);
                    mAdapter.setItems(allPhotoInfos);
                } else if (arg2 == LayoutTypeEnum.PATH_PHOTOS.getValue()) {
                    //Path photos
                    //封装list （标题 + 照片）
                    GridLayoutManager layoutManager = new GridLayoutManager(MainActivity.this, numberOfColumns);
                    layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            if (allPathPhotos.get(position).getLayoutType() == LayoutTypeEnum.PATH_PHOTOS.getValue()) {
                                return numberOfColumns;
                            }
                            return 1;
                        }
                    });
                    mRecyclerView.setLayoutManager(layoutManager);
                    mAdapter.setItems(allPathPhotos);
                    mAdapter.notifyDataSetChanged();
                } else if (arg2 == LayoutTypeEnum.PATH_LIST.getValue()) {
                    //Path list
                    GridLayoutManager layoutManager = new GridLayoutManager(MainActivity.this, 1);
                    mRecyclerView.setLayoutManager(layoutManager);
                    mAdapter.setItems(allPaths);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // set up the RecyclerView
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PhotoAdapter(this, allPhotoInfos);
        mAdapter.setActivity(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemViewCacheSize(30);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        photoLoadTask = new PhotoLoadTask();
        photoLoadTask.execute(LayoutTypeEnum.ALL_PHOTOS.getValue());

        visit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VisitActivity.class);
                startActivity(intent);
            }
        });

        // required by Android 6.0 +
        Boolean isFirstIn = false;
        SharedPreferences pref = getSharedPreferences("myActivityName", 0);
        //取得相应的值，如果没有该值，说明还未写入，用true作为默认值
        isFirstIn = pref.getBoolean("isFirstIn", true);
        if (isFirstIn) {
            initData();
        }
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isFirstIn", false);
        editor.commit();
    }


    private class PhotoLoadTask extends AsyncTask<Integer, Void, List<PhotoInfo>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<PhotoInfo> doInBackground(Integer... integers) {
            PhotoDAO photoDAO = PhotoRoomDatabase.getDatabase(MainActivity.this).photoDAO();
            Integer type = integers[0];
//            List<PhotoInfo> photoInfos = new ArrayList<>();

            allPhotoInfos = photoDAO.queryAllPhotos();
            for (PhotoInfo photoInfo : allPhotoInfos) {
                photoInfo.setLayoutType(LayoutTypeEnum.ALL_PHOTOS.getValue());
            }
            List<PathInfo> pathInfos = photoDAO.queryAllPath();
            for (PathInfo pathInfo : pathInfos) {
                //首先加标题
                PhotoInfo pathP = new PhotoInfo();
                pathP.setTitle(pathInfo.getTitle());
                pathP.setLayoutType(LayoutTypeEnum.PATH_PHOTOS.getValue());
                allPathPhotos.add(pathP);
                String title = pathInfo.getTitle();
                List<PhotoInfo> temp = new ArrayList<>();
                for (PhotoInfo photoInfo : allPhotoInfos) {
                    if (photoInfo.getTitle().equals(title)) {
                        temp.add(photoInfo);
                    }
                }
                //填补空间，凑成3的整数
                if (temp.size() % 3 != 0) {
                    int mod = temp.size() % 3;
                    for (int index = 0; index < 3 - mod; index++) {
                        PhotoInfo emptyPhoto = new PhotoInfo();
                        temp.add(emptyPhoto);
                    }
                }
                //加类型
                for (PhotoInfo photoInfo : temp) {
                    photoInfo.setLayoutType(LayoutTypeEnum.ALL_PHOTOS.getValue());
                }
                allPathPhotos.addAll(temp);
            }
            //加载路线list
            for (PathInfo path : pathInfos) {
                PhotoInfo photoInfo = new PhotoInfo();
                photoInfo.setTitle(path.getTitle());
                photoInfo.setCreateTime(path.getCreateTime());
                photoInfo.setLayoutType(LayoutTypeEnum.PATH_LIST.getValue());
                allPaths.add(photoInfo);
            }
            return allPhotoInfos;
        }


        @Override
        protected void onPostExecute(List<PhotoInfo> photeInfos) {
            mAdapter.setItems(photeInfos);
            mAdapter.notifyDataSetChanged();
        }
    }


    /**
     * check permissions are necessary starting from Android 6
     * if you do not set the permissions, the activity will simply not work and you will be probably baffled for some hours
     * until you find a note on StackOverflow
     */
    private void initData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_EXTERNAL_STORAGE);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return;
        }
        initDate();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initDate();
                }
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            dialog();
        }
        return false;
    }

    protected void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("confirm exit？");
        builder.setTitle("tips");
        builder.setPositiveButton("sure", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                //MainActivity.this.finish;
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 初始化数据
     */
    private void initDate() {
        InputStream is = null;
        // 创建一个带缓冲区的输入流
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        List<PhotoInfo> photoInfos = new ArrayList<>();
        try {
            for (int index = 1; index < 4; index++) {
                //复制照片
                is = getAssets().open("photo" + index + ".jpg");
                String photoName = "outputImage_" + System.currentTimeMillis() + ".jpg";
                File savePath = getExternalFilesDir("photos");
                if (!savePath.exists()) {
                    savePath.mkdir();
                }
                File outputImage = new File(savePath, photoName);
                photoName = outputImage.getAbsolutePath();
                bis = new BufferedInputStream(is);
                bos = new BufferedOutputStream(new FileOutputStream(outputImage));
                // 定义一个字节数组，作为缓冲区
                byte[] buff = new byte[1024];
                int len;
                while ((len = bis.read(buff)) != -1) {
                    bos.write(buff, 0, len); // 从第一个字节开始，向文件写入len个字节
                }

                //复制路线图
                //获取屏幕截图，保存到本地
                is = getAssets().open("path" + index + ".png");
                savePath = getExternalFilesDir("paths");
                if (!savePath.exists()) {
                    savePath.mkdir();
                }
                String pathName = savePath.getAbsolutePath() + "/path_" + System.currentTimeMillis() + ".jpg";
                File outputPath = new File(pathName);
                bis = new BufferedInputStream(is);
                bos = new BufferedOutputStream(new FileOutputStream(outputPath));
                // 定义一个字节数组，作为缓冲区
                byte[] buff1 = new byte[1024];
                int len1;
                while ((len1 = bis.read(buff1)) != -1) {
                    bos.write(buff1, 0, len1); // 从第一个字节开始，向文件写入len个字节
                }

                PhotoInfo photoInfo = new PhotoInfo();
                photoInfo.setTitle("walk to London");
                photoInfo.setPhotoFile(photoName);
                photoInfo.setPathFile(pathName);
                photoInfo.setTemp("22");
                photoInfo.setPressure("1018");
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String createTime = df.format(new Date());
                photoInfo.setCreateTime(createTime);

                photoInfos.add(photoInfo);
            }
            PhotoInsertTask photoInsertTask = new PhotoInsertTask();
            photoInsertTask.execute(photoInfos.toArray(new PhotoInfo[photoInfos.size()]));
        } catch (IOException e) {
            e.printStackTrace();
            try {
                is.close();
                bis.close();
                bos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class PhotoInsertTask extends AsyncTask<PhotoInfo, Void, Integer> {
        @Override
        protected Integer doInBackground(PhotoInfo... photoInfos) {
            PhotoDAO photoDAO = PhotoRoomDatabase.getDatabase(MainActivity.this).photoDAO();
            photoDAO.insertAll(photoInfos);
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            photoLoadTask = new PhotoLoadTask();
            photoLoadTask.execute(LayoutTypeEnum.ALL_PHOTOS.getValue());
        }
    }
}
