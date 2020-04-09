/*
 * Copyright (c) 2017. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.oak.com4510.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import uk.ac.shef.oak.com4510.bean.LayoutTypeEnum;
import uk.ac.shef.oak.com4510.view.PathPhotoActivity;
import uk.ac.shef.oak.com4510.R;
import uk.ac.shef.oak.com4510.view.ShowImageActivity;
import uk.ac.shef.oak.com4510.bean.PhotoInfo;


public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static private Context context;
    private List<PhotoInfo> items;
    private Activity activity;

    public PhotoAdapter(List<PhotoInfo> items) {
        this.items = items;
    }

    public PhotoAdapter(Context cont, List<PhotoInfo> items) {
        super();
        this.items = items;
        context = cont;
    }

    public List<PhotoInfo> getItems() {
        return items;
    }

    public void setItems(List<PhotoInfo> items) {
        this.items = items;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        PhotoAdapter.context = context;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View view = null;
        RecyclerView.ViewHolder holder = null;
        if (viewType == LayoutTypeEnum.ALL_PHOTOS.getValue()) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_list_item,
                    parent, false);
            holder = new PhotoViewHolder(view);
        } else if (viewType == LayoutTypeEnum.PATH_PHOTOS.getValue()) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.titile_list_item,
                    parent, false);
            holder = new TitleViewHolder(view);
        } else if (viewType == LayoutTypeEnum.PATH_LIST.getValue()) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.path_list_item,
                    parent, false);
            holder = new PathViewHolder(view);
        }
        context = parent.getContext();
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        PhotoInfo photoInfo = items.get(position);
        //Use the provided View Holder on the onCreateViewHolder method to populate the
        // current row on the RecyclerView
        if (holder != null && photoInfo != null) {
            if (photoInfo.getLayoutType() == LayoutTypeEnum.ALL_PHOTOS.getValue()) {
                PhotoViewHolder photoViewHolder = (PhotoViewHolder) holder;
                if (photoInfo.getPhotoFile() != null && photoInfo.getPathFile() != null) {

                    if (!isDestroy(activity)) {
                        Glide.with(context).load(new File(photoInfo.getPhotoFile())).into(photoViewHolder.photoImageView);
//                      photoViewHolder.photoImageView.setImageBitmap(BitmapFactory.decodeFile(photoInfo.getPhotoFile()));
                        photoViewHolder.photoImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(context, ShowImageActivity.class);
                                intent.putExtra("photoInfo", photoInfo);
                                context.startActivity(intent);
                            }
                        });
                    }
                } else {
                    photoViewHolder.photoImageView.setImageDrawable(null);
                }
            } else if (photoInfo.getLayoutType() == LayoutTypeEnum.PATH_PHOTOS.getValue()) {
                TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
                titleViewHolder.titleItemTextView.setText(photoInfo.getTitle());
            } else if (photoInfo.getLayoutType() == LayoutTypeEnum.PATH_LIST.getValue()) {
                PathViewHolder pathViewHolder = (PathViewHolder) holder;
                pathViewHolder.pathTextView.setText(photoInfo.getCreateTime() + "   " + photoInfo.getTitle());

                pathViewHolder.pathTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, PathPhotoActivity.class);
                        intent.putExtra("title", photoInfo.getTitle());
                        context.startActivity(intent);
                    }
                });
            }
        }
        //animate(holder);
    }


    // convenience method for getting data at click position
    PhotoInfo getItem(int id) {
        return items.get(id);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position).getLayoutType() == LayoutTypeEnum.ALL_PHOTOS.getValue()) {
            return LayoutTypeEnum.ALL_PHOTOS.getValue();
        } else if (items.get(position).getLayoutType() == LayoutTypeEnum.PATH_PHOTOS.getValue()) {
            return LayoutTypeEnum.PATH_PHOTOS.getValue();
        } else if (items.get(position).getLayoutType() == LayoutTypeEnum.PATH_LIST.getValue()) {
            return LayoutTypeEnum.PATH_LIST.getValue();
        }
        return LayoutTypeEnum.ALL_PHOTOS.getValue();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView;

        PhotoViewHolder(View itemView) {
            super(itemView);
            photoImageView = (ImageView) itemView.findViewById(R.id.image_item);
        }
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView titleItemTextView;

        TitleViewHolder(View itemView) {
            super(itemView);
            titleItemTextView = itemView.findViewById(R.id.title_item);
        }
    }

    class PathViewHolder extends RecyclerView.ViewHolder {
        TextView pathTextView;

        PathViewHolder(View itemView) {
            super(itemView);
            pathTextView = itemView.findViewById(R.id.path_item);
        }
    }

    /**
     * 判断Activity是否Destroy
     *
     * @param mActivity
     * @return
     */
    public static boolean isDestroy(Activity mActivity) {
        if (mActivity == null || mActivity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mActivity.isDestroyed())) {
            return true;
        } else {
            return false;
        }
    }
}