/*
 * Copyright (c) 2018. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.oak.com4510.dataBase;


import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import uk.ac.shef.oak.com4510.bean.PathInfo;
import uk.ac.shef.oak.com4510.bean.PhotoInfo;

@Dao
public interface PhotoDAO {
    @Insert
    void insertAll(PhotoInfo... photeInfos);

    @Insert
    void insert(PhotoInfo photeInfo);

    @Delete
    void delete(PhotoInfo photeInfo);

    @Delete
    void deleteALL(PhotoInfo... photeInfos);

    // it selects a random element
    @Query("SELECT * FROM PhotoInfo WHERE deleteFlag = 0 ORDER BY id DESC LIMIT 1")
    List<PhotoInfo> retrieveOnePhoto();


    @Query("SELECT * FROM PhotoInfo WHERE deleteFlag = 0 ORDER BY id DESC LIMIT 0,100")
    List<PhotoInfo> queryAllPhotos();

    @Query("SELECT * FROM PhotoInfo WHERE title == :title AND deleteFlag = 0 ORDER BY id DESC")
    List<PhotoInfo> queryPhotosByTitle(String title);

    @Query("SELECT DISTINCT title,createTime FROM PhotoInfo WHERE deleteFlag = 0  ORDER BY id DESC")
    List<PathInfo> queryAllPath();

    @Query("SELECT * FROM PhotoInfo WHERE deleteFlag = 0  GROUP BY title  ORDER BY id DESC")
    List<PhotoInfo> queryPathPhotos();

    @Update()
    void updateAllPhotos(PhotoInfo... photeInfos);

    @Query("SELECT * FROM PhotoInfo WHERE photoFile == :photoFile AND deleteFlag = 0 ORDER BY id DESC")
    PhotoInfo queryPhotosByPhotoFile(String photoFile);

}
