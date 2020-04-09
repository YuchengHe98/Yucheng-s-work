package uk.ac.shef.oak.com4510.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import uk.ac.shef.oak.com4510.R;
import uk.ac.shef.oak.com4510.adapter.PhotoAdapter1;
import uk.ac.shef.oak.com4510.bean.PhotoInfo;
import uk.ac.shef.oak.com4510.dataBase.PhotoDAO;
import uk.ac.shef.oak.com4510.dataBase.PhotoRoomDatabase;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class PathPhotoActivity extends AppCompatActivity {

    private TextView pathTitle;

    private Button visit;

    private RecyclerView photoRecyclerView;

    private PhotoAdapter1 photoAdapter;

    private List<PhotoInfo> myPictureList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_photo);

        pathTitle = findViewById(R.id.path_title);
        photoRecyclerView = findViewById(R.id.path_recycler_view);
        visit = findViewById(R.id.path_visit);

        photoRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        photoAdapter = new PhotoAdapter1(this, myPictureList);
        photoAdapter.setActivity(this);
        photoRecyclerView.setAdapter(photoAdapter);
//        photoRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        //获取标题信息，通过标题去索引
        Bundle bundle = getIntent().getExtras();
        String title = bundle.getString("title");

        pathTitle.setText(title);

        PhotoLoadTask photoLoadTask = new PhotoLoadTask();
        photoLoadTask.execute(title);

        visit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PathPhotoActivity.this, VisitActivity.class);
                startActivity(intent);
            }
        });
    }

    private class PhotoLoadTask extends AsyncTask<String, Void, List<PhotoInfo>> {
        @Override
        protected List<PhotoInfo> doInBackground(String... strings) {
            PhotoDAO photoDAO = PhotoRoomDatabase.getDatabase(PathPhotoActivity.this).photoDAO();
            String title = strings[0];
            List<PhotoInfo> photoInfos = photoDAO.queryPhotosByTitle(title);
            return photoInfos;
        }

        @Override
        protected void onPostExecute(List<PhotoInfo> photoInfos) {
            myPictureList = photoInfos;
            photoAdapter.setItems(myPictureList);
            photoAdapter.notifyDataSetChanged();
        }
    }
}
