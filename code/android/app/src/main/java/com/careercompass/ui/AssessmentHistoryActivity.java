package com.careercompass.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.careercompass.R;
import com.careercompass.model.ApiResponse;
import com.careercompass.model.AssessmentHistorySummaryDto;
import com.careercompass.model.AssessmentResultResponse;
import com.careercompass.model.CohortAnalyticsDto;
import com.careercompass.network.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;
import java.util.Map;

public class AssessmentHistoryActivity extends AppCompatActivity {

    private ImageButton btnHistoryBack;
    private ImageButton btnHistoryRefresh;
    private TextView tvTotalAssessmentsCount;
    private TextView tvTopTrendingCareer;
    private LinearLayout layoutCohortAverages;
    private TextView tvHistoryRecordCount;
    private TextView tvEmptyHistory;
    private RecyclerView rvAssessmentHistory;
    private FrameLayout layoutHistoryLoading;
    private TextView tvHistoryLoadingMessage;

    private AssessmentHistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_history);

        bindViews();
        setupRecyclerView();
        setupListeners();
        loadHistoryAndAnalytics();
    }

    private void bindViews() {
        btnHistoryBack = findViewById(R.id.btnHistoryBack);
        btnHistoryRefresh = findViewById(R.id.btnHistoryRefresh);
        tvTotalAssessmentsCount = findViewById(R.id.tvTotalAssessmentsCount);
        tvTopTrendingCareer = findViewById(R.id.tvTopTrendingCareer);
        layoutCohortAverages = findViewById(R.id.layoutCohortAverages);
        tvHistoryRecordCount = findViewById(R.id.tvHistoryRecordCount);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        rvAssessmentHistory = findViewById(R.id.rvAssessmentHistory);
        layoutHistoryLoading = findViewById(R.id.layoutHistoryLoading);
        tvHistoryLoadingMessage = findViewById(R.id.tvHistoryLoadingMessage);
    }

    private void setupRecyclerView() {
        rvAssessmentHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new AssessmentHistoryAdapter(this::openAssessmentDetails);
        rvAssessmentHistory.setAdapter(historyAdapter);
    }

    private void setupListeners() {
        btnHistoryBack.setOnClickListener(v -> finish());
        btnHistoryRefresh.setOnClickListener(v -> loadHistoryAndAnalytics());
    }

    private void loadHistoryAndAnalytics() {
        showLoading("Fetching assessment records and cohort insights…");

        // 1. Fetch History List
        ApiClient.getService().getAssessmentHistory().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AssessmentHistorySummaryDto>>> call,
                                   Response<ApiResponse<List<AssessmentHistorySummaryDto>>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<AssessmentHistorySummaryDto> list = response.body().getData();
                    historyAdapter.setItems(list);
                    tvHistoryRecordCount.setText(list.size() + " records");
                    tvEmptyHistory.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(AssessmentHistoryActivity.this,
                            "Failed to load history (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AssessmentHistorySummaryDto>>> call, Throwable t) {
                hideLoading();
                showErrorDialog("Cannot connect to backend server at " + ApiClient.getBaseUrl() +
                        "\n\nPlease ensure Spring Boot is running and reachable.\n\nError: " + t.getMessage());
            }
        });

        // 2. Fetch Cohort Analytics
        ApiClient.getService().getCohortAnalytics().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<CohortAnalyticsDto>> call,
                                   Response<ApiResponse<CohortAnalyticsDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    displayCohortAnalytics(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CohortAnalyticsDto>> call, Throwable t) {
                // Non-critical, ignore silent failure
            }
        });
    }

    private void displayCohortAnalytics(CohortAnalyticsDto analytics) {
        tvTotalAssessmentsCount.setText(analytics.getTotalAssessments() + " Done");

        if (analytics.getMostCommonCareerCount() > 0) {
            tvTopTrendingCareer.setText(analytics.getMostCommonCareer() + " (" + analytics.getMostCommonCareerCount() + ")");
        } else {
            tvTopTrendingCareer.setText("None yet");
        }

        // Render average scores per dimension
        layoutCohortAverages.removeAllViews();
        Map<String, Double> averages = analytics.getDimensionAverages();
        if (averages != null && !averages.isEmpty()) {
            for (Map.Entry<String, Double> entry : averages.entrySet()) {
                View row = getLayoutInflater().inflate(R.layout.item_trait_score, layoutCohortAverages, false);

                TextView tvName = row.findViewById(R.id.tvTraitDimensionName);
                TextView tvScore = row.findViewById(R.id.tvTraitScorePercent);
                ProgressBar pb = row.findViewById(R.id.pbTraitScore);

                tvName.setText(entry.getKey());
                int scoreVal = (int) Math.round(entry.getValue());
                tvScore.setText(scoreVal + "% Avg");
                pb.setProgress(scoreVal);

                layoutCohortAverages.addView(row);
            }
        }
    }

    private void openAssessmentDetails(AssessmentHistorySummaryDto item) {
        showLoading("Loading complete assessment report for " + item.getStudentName() + "…");

        ApiClient.getService().getAssessmentResult(item.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<AssessmentResultResponse>> call,
                                   Response<ApiResponse<AssessmentResultResponse>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Intent intent = new Intent(AssessmentHistoryActivity.this, ResultsActivity.class);
                    intent.putExtra(AssessmentActivity.EXTRA_RESULT_DATA, response.body().getData());
                    startActivity(intent);
                } else {
                    Toast.makeText(AssessmentHistoryActivity.this,
                            "Failed to load full report (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AssessmentResultResponse>> call, Throwable t) {
                hideLoading();
                Toast.makeText(AssessmentHistoryActivity.this,
                        "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(String msg) {
        if (tvHistoryLoadingMessage != null) tvHistoryLoadingMessage.setText(msg);
        if (layoutHistoryLoading != null) layoutHistoryLoading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        if (layoutHistoryLoading != null) layoutHistoryLoading.setVisibility(View.GONE);
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Connection Notice")
                .setMessage(message)
                .setPositiveButton("Retry", (dialog, which) -> loadHistoryAndAnalytics())
                .setNeutralButton("Change Server IP", (dialog, which) -> showChangeUrlDialog())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    private void showChangeUrlDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setText(ApiClient.getBaseUrl());
        input.setSelection(input.getText().length());
        input.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(this)
                .setTitle("Configure Server URL")
                .setMessage("Current host address:\n- Emulator: http://10.0.2.2:8080/api/\n- Physical Phone: http://10.173.188.75:8080/api/\n\nEdit URL below:")
                .setView(input)
                .setPositiveButton("Save & Connect", (d, w) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        ApiClient.setBaseUrl(url);
                        loadHistoryAndAnalytics();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
