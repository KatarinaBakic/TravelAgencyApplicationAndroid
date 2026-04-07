package com.example.travelagencyapplication.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelagencyapplication.DestinationDetailActivity;
import com.example.travelagencyapplication.R;
import com.example.travelagencyapplication.model.TourPackage;
import com.example.travelagencyapplication.model.TourPackageDTO;

import java.util.List;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {
    private List<TourPackageDTO> listDestinations;
    private static final String BASE_URL = "http://192.168.1.9:8080";

    public DestinationAdapter(List<TourPackageDTO> list) { this.listDestinations = list; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_destination_user_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TourPackageDTO item = listDestinations.get(position);

        // --- LOGOVANJE CELOG OBJEKTA ---
        com.google.gson.Gson gson = new com.google.gson.Gson();
        String jsonObjekat = gson.toJson(item);
        android.util.Log.d("PROVERA_ITEMA", "Ceo objekat: " + jsonObjekat);
        // ------------------------------


        holder.title.setText(item.getTitle());
        holder.desc.setText(item.getDescription());
        holder.price.setText("Cena: " + item.getPrice() + " EUR");

        String path = item.getImageUrl();

        if (path != null) {
            String imageUrl = BASE_URL + path;
          // Log.d("SLIKA_URL1", "Pokušavam da učitam: " + imageUrl);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.default_url_1)
                    .error(android.R.drawable.stat_notify_error)
                    .centerCrop()
                    .into(holder.ivDestination);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DestinationDetailActivity.class);
                intent.putExtra("packageTourId", item.getId());

                int position = holder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    TourPackageDTO clickedItem = listDestinations.get(position);
                }
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listDestinations != null ? listDestinations.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, desc, price;
        ImageView ivDestination;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            desc  = itemView.findViewById(R.id.tvDescription);
            price = itemView.findViewById(R.id.tvPrice);
            ivDestination = itemView.findViewById(R.id.ivDestination);
        }
    }

    public void updateList(List<TourPackageDTO> newList) {
        this.listDestinations = newList;
        notifyDataSetChanged(); // Ovo govori listi: "Hej, podaci su se promenili, nacrtaj ih ponovo!"
    }
}