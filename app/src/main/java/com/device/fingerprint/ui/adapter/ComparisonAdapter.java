package com.device.fingerprint.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.device.fingerprint.R;
import com.device.fingerprint.model.FieldDiff;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter that groups field diffs by dimension with expandable headers.
 * Each dimension can be tapped to expand/collapse its field list.
 */
public class ComparisonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_FIELD  = 1;

    private final List<Row> rows = new ArrayList<>();

    public ComparisonAdapter(List<FieldDiff> diffs) {
        buildRows(diffs);
    }

    private void buildRows(List<FieldDiff> diffs) {
        rows.clear();
        Map<String, List<FieldDiff>> grouped = new LinkedHashMap<>();
        for (FieldDiff d : diffs) {
            grouped.computeIfAbsent(d.getDimension(), k -> new ArrayList<>()).add(d);
        }
        for (Map.Entry<String, List<FieldDiff>> entry : grouped.entrySet()) {
            String dim = entry.getKey();
            List<FieldDiff> fields = entry.getValue();
            int match = 0;
            for (FieldDiff f : fields) if (f.isMatched()) match++;
            double pct = fields.size() > 0 ? match * 100.0 / fields.size() : 0;

            DimHeader header = new DimHeader(dim, pct, match, fields.size());
            rows.add(new Row(header, null));
            for (FieldDiff f : fields) {
                rows.add(new Row(header, f));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).field == null ? TYPE_HEADER : TYPE_FIELD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comparison_header, parent, false);
            return new HeaderVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_comparison_field, parent, false);
            return new FieldVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        Row row = rows.get(position);
        if (getItemViewType(position) == TYPE_HEADER) {
            ((HeaderVH) h).bind(row.header, position);
        } else {
            boolean visible = row.header.expanded;
            ((FieldVH) h).bind(row.field, visible);
        }
    }

    @Override
    public int getItemCount() { return rows.size(); }

    // ---------- data structures ----------

    private static class DimHeader {
        String dimension;
        double pct;
        int match, total;
        boolean expanded = true;
        DimHeader(String d, double p, int m, int t) { dimension=d; pct=p; match=m; total=t; }
    }

    private static class Row {
        DimHeader header;
        FieldDiff field;
        Row(DimHeader h, FieldDiff f) { header=h; field=f; }
    }

    // ---------- view holders ----------

    class HeaderVH extends RecyclerView.ViewHolder {
        View colorBar;
        TextView tvDim, tvScore;
        ImageView ivArrow;
        HeaderVH(View v) {
            super(v);
            colorBar = v.findViewById(R.id.color_bar);
            tvDim    = v.findViewById(R.id.tv_dimension);
            tvScore  = v.findViewById(R.id.tv_score);
            ivArrow  = v.findViewById(R.id.iv_arrow);
            v.setOnClickListener(view -> {
                int pos = getBindingAdapterPosition();
                if (pos < 0) return;
                Row row = rows.get(pos);
                row.header.expanded = !row.header.expanded;
                notifyItemRangeChanged(pos, rows.size() - pos);
            });
        }
        void bind(DimHeader h, int pos) {
            tvDim.setText(h.dimension + " Similarity");
            tvScore.setText(String.format(java.util.Locale.getDefault(), "%.0f%% (%d/%d)", h.pct, h.match, h.total));
            int c = scoreColor(h.pct);
            colorBar.setBackgroundColor(c);
            tvScore.setTextColor(c);
            ivArrow.setRotation(h.expanded ? 0 : -90);
        }
    }

    static class FieldVH extends RecyclerView.ViewHolder {
        TextView tvName, tvA, tvB, tvMatch;
        FieldVH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_field_name);
            tvA    = v.findViewById(R.id.tv_value_a);
            tvB    = v.findViewById(R.id.tv_value_b);
            tvMatch= v.findViewById(R.id.tv_match);
        }
        void bind(FieldDiff d, boolean visible) {
            itemView.setVisibility(visible ? View.VISIBLE : View.GONE);
            itemView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    visible ? ViewGroup.LayoutParams.WRAP_CONTENT : 0));
            if (!visible) return;

            tvName.setText(d.getDisplayName());
            tvA.setText("A: " + d.getDisplayValueA());
            tvB.setText("B: " + d.getDisplayValueB());
            if (d.isMatched()) {
                tvMatch.setText("MATCH");
                tvMatch.setTextColor(0xFF4CAF50);
            } else {
                tvMatch.setText("DIFF");
                tvMatch.setTextColor(0xFFF44336);
            }
        }
    }

    private static int scoreColor(double pct) {
        if (pct >= 90) return 0xFF4CAF50;
        if (pct >= 75) return 0xFF8BC34A;
        if (pct >= 60) return 0xFFFFC107;
        if (pct >= 40) return 0xFFFF9800;
        return 0xFFF44336;
    }
}
