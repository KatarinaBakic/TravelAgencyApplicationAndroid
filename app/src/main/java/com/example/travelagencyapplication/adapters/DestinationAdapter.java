package com.example.travelagencyapplication.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelagencyapplication.DestinationDetailActivity;
import com.example.travelagencyapplication.EditDestinationActivity;
import com.example.travelagencyapplication.R;
import com.example.travelagencyapplication.api.ApiService;
import com.example.travelagencyapplication.api.RetrofitClient;
import com.example.travelagencyapplication.model.TourPackageDTO;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {
    private List<TourPackageDTO> listDestinations;
    private String userRole;

    public DestinationAdapter(List<TourPackageDTO> list, String userRole) {
        this.listDestinations = list;
        this.userRole         = userRole;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_destination_user_page, parent, false);
        return new ViewHolder(view);
    }

    //TODO: proveriti position!!!!!!!!!!!!!!!!!!!!!!
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        TourPackageDTO currentPackage = listDestinations.get(position);
        if ("ROLE_ADMIN".equals(userRole)) {
            if (currentPackage.getIsDeleted() == 1) {
                holder.itemView.setAlpha(0.5f); // Prozirno
                holder.btnDelete.setVisibility(View.GONE); 
                holder.btnEdit.setVisibility(View.GONE);
                holder.btnRestore.setVisibility(View.VISIBLE);
                holder.btnRestore.setOnClickListener(v -> {
                    restorePackage(v.getContext(), currentPackage.getId(), position);
                });
                holder.itemView.setOnClickListener(v -> {
                    showRestoreDialog(v.getContext(), currentPackage.getId(), position);
                });
            } else{
                holder.itemView.setAlpha(1.0f);
                holder.btnDelete.setVisibility(View.VISIBLE);
                holder.btnEdit.setVisibility(View.VISIBLE);
                holder.btnRestore.setVisibility(View.GONE);
                holder.btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Brisanje aranžmana")
                            .setMessage("Da li ste sigurni da želite da obrišete '" + currentPackage.getTitle() + "'?")
                            .setPositiveButton("Obriši", (dialog, which) -> {
                                ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
                                apiService.deleteDestination(currentPackage.getId()).enqueue(new Callback<Map<String, Object>>() {
                                    @Override
                                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                                        if (response.isSuccessful()) {
                                            listDestinations.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, listDestinations.size());

                                            Toast.makeText(v.getContext(), "Aranžman prebačen u obrisane!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(v.getContext(), "Greška pri brisanju", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                                        Log.e("DELETE_ERROR", t.getMessage());
                                    }
                                });
                            })
                            .setNegativeButton("Otkaži", null)
                            .show();
                });
                holder.btnEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), EditDestinationActivity.class);
                    intent.putExtra("destinationId", currentPackage.getId());
                    intent.putExtra("title", currentPackage.getTitle());
                    intent.putExtra("desc", currentPackage.getDescription());
                    intent.putExtra("price", (double) currentPackage.getPrice());
                    intent.putExtra("imageUrl", currentPackage.getImageUrl());
                    intent.putExtra("latitude", currentPackage.getLatitude());
                    intent.putExtra("longitude", currentPackage.getLongitude());

                    if (currentPackage.getCategoryId() != null) {
                        intent.putExtra("categoryId", (long) currentPackage.getCategoryId());
                    } else {
                        intent.putExtra("categoryId", -1L);
                    }
                    v.getContext().startActivity(intent);
                });
            }
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnRestore.setVisibility(View.GONE);
        }
        holder.title.setText(currentPackage.getTitle());
        holder.desc.setText(currentPackage.getDescription());
        holder.price.setText("Cena: " + currentPackage.getPrice() + " EUR");

        String path = currentPackage.getImageUrl();
        if (path != null) {
            String imageUrl = RetrofitClient.SERVER_URL + path;
         //   Log.e("imageUrl", imageUrl);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .signature(new com.bumptech.glide.signature.ObjectKey(currentPackage.getId()))
                   // .diskCacheStrategy(DiskCacheStrategy.NONE) //za proveru
                   // .skipMemoryCache(true)
                    .placeholder(R.drawable.default_url_1)
                    .error(android.R.drawable.stat_notify_error)
                    .centerCrop()
                    .into(holder.ivDestination);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DestinationDetailActivity.class);
                intent.putExtra("packageTourId", currentPackage.getId());

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
        ImageButton btnEdit, btnDelete, btnRestore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title         = itemView.findViewById(R.id.tvTitle);
            desc          = itemView.findViewById(R.id.tvDescription);
            price         = itemView.findViewById(R.id.tvPrice);
            ivDestination = itemView.findViewById(R.id.ivDestination);
            btnEdit       = itemView.findViewById(R.id.btnEditDest);
            btnDelete     = itemView.findViewById(R.id.btnDeleteDest);
            btnRestore    = itemView.findViewById(R.id.btnRestore);
        }
    }
    public void updateList(List<TourPackageDTO> newList) {
        this.listDestinations = newList;
        notifyDataSetChanged();
    }
    private void showRestoreDialog(Context context, Long id, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Vraćanje aranžmana")
                .setMessage("Da li želite da ovaj aranžman ponovo postane aktivan?")
                .setPositiveButton("Vrati", (dialog, which) -> {
                    ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
                    apiService.restorePackage(id).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                listDestinations.remove(position);
                                notifyDataSetChanged();
                                Toast.makeText(context, "Aranžman je ponovo aktivan!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
                    });
                })
                .setNegativeButton("Otkaži", null)
                .show();
    }
    private void restorePackage(Context context, Long id, int position) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        apiService.restorePackage(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    if (position >= 0 && position < listDestinations.size()) {
                        listDestinations.remove(position);
                        notifyDataSetChanged();
                    }
                    Toast.makeText(context, "Aranžman vraćen u ponudu!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Greška pri vraćanju", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("RESTORE_ERROR", t.getMessage());
            }
        });
    }
}