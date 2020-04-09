///*
// * Copyright (c) 2017. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
// */
package uk.ac.shef.oak.com4510.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import uk.ac.shef.oak.com4510.R;
import uk.ac.shef.oak.com4510.bean.PhotoInfo;
import uk.ac.shef.oak.com4510.util.BitmapUtil;

/**
 * 展示图片的activity
 */
public class ShowImageActivity extends Activity {

    private ImageView photoView;

    private ImageView pathView;

    private TextView title;

    private TextView temp;

    private TextView pressure;

    private LinearLayout photoLinearLayout;

    private PhotoInfo photoInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        photoView = findViewById(R.id.photo_view);
        pathView = findViewById(R.id.path_view);
        title = findViewById(R.id.show_title);
        temp = findViewById(R.id.show_temp);
        pressure = findViewById(R.id.show_pressure);
        photoLinearLayout = findViewById(R.id.photo_layout);

        //获取图片的信息
        photoInfo = (PhotoInfo) getIntent().getSerializableExtra("photoInfo");

        //根据相关信息设置页面
        title.setText("Title:" + photoInfo.getTitle());
        temp.setText("Temp:" + photoInfo.getTemp() + "°C");
        pressure.setText("Pressure:" + photoInfo.getPressure() + "hPa");

        //设置图片
        Glide.with(this).load(photoInfo.getPhotoFile()).into(photoView);

        //设置路线图
        Glide.with(this).load(photoInfo.getPathFile()).into(pathView);
    }

    /**
     * 使用Matrix
     *
     * @param bitmap 原始的Bitmap
     * @param width  目标宽度
     * @param height 目标高度
     * @return 缩放后的Bitmap
     */
    public static Bitmap scaleMatrix(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float scaleW = (float) (width * 1.0 / w);
        float scaleH = (float) (height * 1.0 / h);
        Matrix matrix = new Matrix();
        matrix.postScale(scaleW, scaleH); // 长和宽放大缩小的比例
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }
}
