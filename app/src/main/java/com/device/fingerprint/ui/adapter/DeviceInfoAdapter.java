package com.device.fingerprint.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.device.fingerprint.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying device information in categorized sections
 */
public class DeviceInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    
    private List<Object> items; // Mixed list of Section headers and Row items
    
    public DeviceInfoAdapter(List<Section> sections) {
        this.items = new ArrayList<>();
        for (Section section : sections) {
            items.add(section); // Header
            items.addAll(section.getItems()); // Items
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof Section ? TYPE_HEADER : TYPE_ITEM;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_info_row, parent, false);
            return new ItemViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            Section section = (Section) items.get(position);
            ((HeaderViewHolder) holder).bind(section.getTitle());
        } else {
            RowItem item = (RowItem) items.get(position);
            ((ItemViewHolder) holder).bind(item);
        }
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_section_title);
        }
        
        void bind(String title) {
            tvTitle.setText(title);
        }
    }
    
    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvKey;
        TextView tvValue;
        
        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKey = itemView.findViewById(R.id.tv_key);
            tvValue = itemView.findViewById(R.id.tv_value);
        }
        
        void bind(RowItem item) {
            tvKey.setText(item.getKey());
            tvValue.setText(item.getValue());
        }
    }
    
    /**
     * Section containing a header and list of items
     */
    public static class Section {
        private String title;
        private List<RowItem> items;
        
        public Section(String title) {
            this.title = title;
            this.items = new ArrayList<>();
        }
        
        public void addItem(String key, String value) {
            items.add(new RowItem(key, value));
        }
        
        public String getTitle() { return title; }
        public List<RowItem> getItems() { return items; }
    }
    
    /**
     * Individual key-value row
     */
    public static class RowItem {
        private String key;
        private String value;
        
        public RowItem(String key, String value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() { return key; }
        public String getValue() { return value; }
    }
}
