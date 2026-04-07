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
import com.example.travelagencyapplication.model.TourPackage;
import com.example.travelagencyapplication.model.TourPackageDTO;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<CategoryDTO> categoryList;
    private Context context;

    public CategoryAdapter(List<CategoryDTO> categoryList, Context context) {
        this.categoryList = categoryList;
        this.context = context;
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

        // 1. Dobijanje liste aranžmana za tu konkretnu kategoriju
        List<TourPackageDTO> packages = category.getTourpackages();

        android.util.Log.d("PROVERA", "Kategorija: " + category.getName() + " ima aranžmana: " + (packages != null ? packages.size() : "NULL"));

        // 2. Provera da lista nije null (da aplikacija ne pukne)
        if (packages == null) {
            packages = new ArrayList<>();
        }

        // 3. KREIRANJE UNUTRAŠNJEG ADAPTERA (Ovo ti verovatno fali ili je pogrešno)
        DestinationAdapter destinationAdapter = new DestinationAdapter(packages);

        // 4. POSTAVLJANJE HORIZONTALNOG LAYOUT MENADŽERA
        // Bez ove linije RecyclerView ne zna kako da poređa stavke i ostaje nevidljiv!
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(
                holder.itemView.getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        );
        holder.rvTourPackages.setLayoutManager(horizontalLayoutManager);

        // 5. POVEZIVANJE ADAPTERA
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