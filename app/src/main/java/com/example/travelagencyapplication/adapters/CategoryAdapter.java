package com.example.travelagencyapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelagencyapplication.R;
import com.example.travelagencyapplication.model.CategoryDTO;
import com.example.travelagencyapplication.model.TourPackageDTO;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<CategoryDTO> categoryList;
    private Context context;
    private String userRole;

    public CategoryAdapter(List<CategoryDTO> categoryList, Context context, String userRole) {
        this.categoryList = categoryList;
        this.context      = context;
        this.userRole     = userRole;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryDTO category = categoryList.get(position);
        holder.tvCategoryName.setText(category.getName());
        List<TourPackageDTO> packages = category.getTourpackages();
        if (packages == null) {
            packages = new ArrayList<>();
        }
        DestinationAdapter destinationAdapter = new DestinationAdapter(packages, userRole);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(
                holder.itemView.getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );

        holder.rvTourPackages.setLayoutManager(horizontalLayoutManager);
        holder.rvTourPackages.setAdapter(destinationAdapter);
    }

    @Override
    public int getItemCount() { return categoryList.size(); }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        RecyclerView rvTourPackages;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            rvTourPackages = itemView.findViewById(R.id.rvTourPackages);
        }
    }
}