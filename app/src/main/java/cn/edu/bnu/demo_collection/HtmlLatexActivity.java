package cn.edu.bnu.demo_collection;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @CreateDate: 2025/6/9
 * @author: jingjie
 * @desc: 系列课随堂测
 */

public class HtmlLatexActivity extends AppCompatActivity {
    private TextView tvRefresh;
    private RecyclerView rvList;
    private static final String TAG = "SeriesCoursesClassTestingActivity";

    private List<String> data = new ArrayList<>();
    private HtmlLatexAdapter stringAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html_latex);
        rvList = findViewById(R.id.rv_list);

        tvRefresh = findViewById(R.id.tv_refresh);

        tvRefresh.setText("刷新 1");

        tvRefresh.setOnClickListener(v -> {
            getData();
        });

        stringAdapter = new HtmlLatexAdapter(this, data);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvList.setAdapter(stringAdapter);

        rvList.setItemViewCacheSize(0);

        getData();
    }

    private void getData() {
        ServiceGenerator.createServiceApi(Api.class)
                .mockListLatex()
                .enqueue(new Callback<List<String>>() {
                    @Override
                    public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            data.clear();
                            data.addAll(response.body());
                            stringAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<String>> call, Throwable t) {
                        Toast.makeText(HtmlLatexActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}