package com.device.fingerprint.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.device.fingerprint.R;
import com.device.fingerprint.model.DeviceInfo;

import java.util.List;

/**
 * Adapter for displaying device history list
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    
    private List<DeviceInfo> devices;
    private OnItemClickListener listener;
    
    public HistoryAdapter(List<DeviceInfo> devices, OnItemClickListener listener) {
        this.devices = devices;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeviceInfo device = devices.get(position);
        holder.bind(device, listener);
    }
    
    @Override
    public int getItemCount() {
        return devices.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvDeviceName;
        TextView tvAndroidVersion;
        TextView tvFingerprint;
        TextView tvCollectionTime;
        TextView tvFeatureCount;
        View riskIndicator;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            tvAndroidVersion = itemView.findViewById(R.id.tv_android_version);
            tvFingerprint = itemView.findViewById(R.id.tv_fingerprint);
            tvCollectionTime = itemView.findViewById(R.id.tv_collection_time);
            tvFeatureCount = itemView.findViewById(R.id.tv_feature_count);
            riskIndicator = itemView.findViewById(R.id.risk_indicator);
        }
        
        void bind(DeviceInfo device, OnItemClickListener listener) {
            tvDeviceName.setText(String.format("%s %s", device.getBrand(), device.getModel()));
            tvAndroidVersion.setText(String.format("Android %s (API %d)", 
                    device.getOsVersion(), device.getSdkInt()));
            tvFingerprint.setText(device.getFingerprint() != null ? 
                    device.getFingerprint().toUpperCase() : "N/A");
            tvCollectionTime.setText(device.getCollectionTime() != null ? 
                    device.getCollectionTime() : "Unknown");
            tvFeatureCount.setText(String.format("%d features", 
                    device.getRawFeatures().size()));
            
            // Risk indicator
            int riskColor = getRiskColor(device.getRiskScore());
            riskIndicator.setBackgroundColor(riskColor);
            
            cardView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(device);
            });
            
            cardView.setOnLongClickListener(v -> {
                if (listener != null) listener.onItemLongClick(device);
                return true;
            });
        }
        
        private int getRiskColor(double riskScore) {
            if (riskScore >= 70) return 0xFFF44336;
            if (riskScore >= 50) return 0xFFFF9800;
            if (riskScore >= 30) return 0xFFFFC107;
            if (riskScore >= 10) return 0xFF4CAF50;
            return 0xFF2E7D32;
        }
    }
    
    public interface OnItemClickListener {
        void onItemClick(DeviceInfo device);
        void onItemLongClick(DeviceInfo device);
    }
}
