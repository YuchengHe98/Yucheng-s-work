package uk.ac.shef.oak.com4510.view;

import androidx.appcompat.app.AppCompatActivity;
import uk.ac.shef.oak.com4510.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class VisitActivity extends AppCompatActivity {

    private EditText visitTitle;

    private Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit);

        visitTitle = findViewById(R.id.visit_title);
        start = findViewById(R.id.visit_start);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titleContent = visitTitle.getText().toString();
                if (titleContent == null || "".equals(titleContent)) {
                    //标题为空，不允许开始，弹toast
                    Toast toast = Toast.makeText(VisitActivity.this, "Title is empty,can not start!!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    //跳转到地图页面
                    Intent intent = new Intent(VisitActivity.this, MapsActivity.class);
                    intent.putExtra("title", titleContent);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
