package com.careercompass.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.careercompass.R;
import com.careercompass.model.AssessmentHistorySummaryDto;
import java.util.ArrayList;
import java.util.List;

public class AssessmentHistoryAdapter extends RecyclerView.Adapter<AssessmentHistoryAdapter.ViewHolder> {

    public interface OnAssessmentClickListener {
        void onAssessmentClick(AssessmentHistorySummaryDto item);
    }

    private final List<AssessmentHistorySummaryDto> items = new ArrayList<>();
    private final OnAssessmentClickListener listener;

    public AssessmentHistoryAdapter(OnAssessmentClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<AssessmentHistorySummaryDto> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assessment_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AssessmentHistorySummaryDto item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvStudentName;
        private final TextView tvEducation;
        private final TextView tvDate;
        private final TextView tvTopCareer;
        private final TextView tvTopTrait;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvHistoryStudentName);
            tvEducation = itemView.findViewById(R.id.tvHistoryEducation);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvTopCareer = itemView.findViewById(R.id.tvHistoryTopCareer);
            tvTopTrait = itemView.findViewById(R.id.tvHistoryTopTrait);
        }

        void bind(AssessmentHistorySummaryDto item, OnAssessmentClickListener listener) {
            tvStudentName.setText(item.getStudentName());
            tvEducation.setText(!item.getEducationLevel().isEmpty() ? item.getEducationLevel() : "Student");
            tvDate.setText("🕒 " + item.getFormattedDate());

            String careerText = item.getTopCareerName() + " (" + item.getTopMatchScore() + "% Match)";
            tvTopCareer.setText(careerText);

            String traitText = "Top Dimension: " + item.getDominantDimension() + " (" + item.getDominantDimensionScore() + "%)";
            tvTopTrait.setText(traitText);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAssessmentClick(item);
                }
            });
        }
    }
}
