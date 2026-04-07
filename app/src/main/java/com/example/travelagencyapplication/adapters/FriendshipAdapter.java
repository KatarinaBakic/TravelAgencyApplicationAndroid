package com.example.travelagencyapplication.adapters; // Proveri da li je ovo tvoj folder

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelagencyapplication.R;
import com.example.travelagencyapplication.model.Friendship;

import java.util.List;

public class FriendshipAdapter extends RecyclerView.Adapter<FriendshipAdapter.FriendViewHolder> {

    private List<Friendship> friendshipList;
    private String type; // "SENT", "ACTIVE", "PENDING"
    private OnFriendshipActionListener listener;

    // Interfejs za klik na dugmiće
    public interface OnFriendshipActionListener {
        void onAction(Friendship friendship, String actionType);
    }

    public FriendshipAdapter(List<Friendship> friendshipList, String type, OnFriendshipActionListener listener) {
        this.friendshipList = friendshipList;
        this.type = type;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ovde koristimo onaj item_friendship.xml koji smo ranije napravili
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friendship, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friendship f = friendshipList.get(position);
        String displayName = "";

        if (type.equals("SENT")) {
            // Ako smo mi poslali, zanima nas ime onoga ko prima (user2)
            if (f.getUser2() != null) {
                displayName = f.getUser2().getUsername();
            }
        } else {
            // Ako primamo ili smo već prijatelji, zanima nas ko je poslao (user1)
            if (f.getUser1() != null) {
                displayName = f.getUser1().getUsername();
            }
        }

        holder.tvUsername.setText(displayName != null ? displayName : "Nepoznat korisnik");

        holder.btnAction.setOnClickListener(v -> {
            if (type.equals("SENT")) {
                android.util.Log.d("KLIK_DEBUG", "Kliknuto na: " + type + " za ID: " + f.getId());
                listener.onAction(f, "CANCEL");
            } else if (type.equals("ACTIVE")) {
                android.util.Log.d("KLIK_DEBUG", "Kliknuto na: " + type + " za ID: " + f.getId());
                listener.onAction(f, "REMOVE");
            } else if (type.equals("PENDING")) {
                android.util.Log.d("KLIK_DEBUG", "Kliknuto na: " + type + " za ID: " + f.getId());
                listener.onAction(f, "ACCEPT");
            }
        });

        // Postavljanje teksta na dugmetu
        if (type.equals("SENT")) holder.btnAction.setText("Otkaži");
        else if (type.equals("ACTIVE")) holder.btnAction.setText("Ukloni");
        else if (type.equals("PENDING")) holder.btnAction.setText("Prihvati");
    }

    @Override
    public int getItemCount() {
        return friendshipList != null ? friendshipList.size() : 0;
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        Button btnAction;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvFriendName);
            btnAction = itemView.findViewById(R.id.btnFriendAction);
        }
    }
}