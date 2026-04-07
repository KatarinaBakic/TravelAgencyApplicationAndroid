package com.example.travelagencyapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelagencyapplication.adapters.FriendshipAdapter;
import com.example.travelagencyapplication.api.ApiService;
import com.example.travelagencyapplication.api.RetrofitClient;
import com.example.travelagencyapplication.model.Friendship;
import com.example.travelagencyapplication.model.User;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsActivity extends AppCompatActivity {
    private Spinner spinnerAvailableUsers;
    private RecyclerView rvSent, rvReceived, rvActive;
    private FriendshipAdapter adapterSent, adapterReceived, adapterActive;
    private ApiService apiService;
    private long currentUserId;
    private List<User> availableUsersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friends);

        // 1. Inicijalizacija API servisa i korisnika
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        SharedPreferences sp = getSharedPreferences("TravelAgencyApplication", MODE_PRIVATE);
        currentUserId = Long.parseLong(sp.getString("userId", "-1"));

        // 2. Povezivanje UI elemenata
        spinnerAvailableUsers = findViewById(R.id.spinnerUsers);
        rvSent     = findViewById(R.id.rvSentRequests);
        rvReceived = findViewById(R.id.rvPendingRequests);
        rvActive   = findViewById(R.id.rvActiveFriends);

        // Postavljanje LayoutManagera za RecyclerView-ove
        rvSent.setLayoutManager(new LinearLayoutManager(this));
        rvReceived.setLayoutManager(new LinearLayoutManager(this));
        rvActive.setLayoutManager(new LinearLayoutManager(this));

        // Dugme za slanje zahteva iz Spinnera
        findViewById(R.id.btnSendRequest).setOnClickListener(v -> sendFriendRequest());

        // 3. Učitavanje svih podataka
        loadAllData();
    }

    private void loadAllData() {
        loadAvailableUsers();
        loadSentRequests();
        loadReceivedRequests();
        loadActiveFriends();
    }

    private void loadAvailableUsers() {
        apiService.getAvailableUsers(currentUserId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    availableUsersList = response.body();
                    List<String> userNames = new ArrayList<>();
                    userNames.add("Izaberi korisnika..."); // Placeholder

                    for (User u : availableUsersList) {
                        userNames.add(u.getUsername()); // Ili u.getName() zavisi od tvog User modela
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(FriendsActivity.this,
                            android.R.layout.simple_spinner_item, userNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerAvailableUsers.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e("API_ERROR", "Greška Spinner: " + t.getMessage());
            }
        });
    }

    private void loadSentRequests() {
        apiService.getSentRequests(currentUserId).enqueue(new Callback<List<Friendship>>() {
            @Override
            public void onResponse(Call<List<Friendship>> call, Response<List<Friendship>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapterSent = new FriendshipAdapter(response.body(), "SENT", (f, action) -> {
                        if (action.equals("CANCEL")) {
                            // Koristimo ID reda iz baze
                            removeFriend(f.getId());
                        }
                    });
                    rvSent.setAdapter(adapterSent);
                }
            }
            @Override public void onFailure(Call<List<Friendship>> call, Throwable t) {}
        });
    }

    private void loadReceivedRequests() {
        apiService.getReceivedRequests(currentUserId).enqueue(new Callback<List<Friendship>>() {
            @Override
            public void onResponse(Call<List<Friendship>> call, Response<List<Friendship>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapterReceived = new FriendshipAdapter(response.body(), "PENDING", (f, action) -> {
                        if (action.equals("ACCEPT")) {
                            acceptFriendRequest(f.getId());
                        } else if (action.equals("REJECT")) {
                            // Dodajemo i opciju za odbijanje
                            rejectFriendRequest(f.getId());
                        }
                    });
                    rvReceived.setAdapter(adapterReceived);
                }
            }
            @Override public void onFailure(Call<List<Friendship>> call, Throwable t) {}
        });
    }

    private void acceptFriendRequest(long requestId) {
        apiService.acceptRequest(requestId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FriendsActivity.this, "Zahtev prihvaćen!", Toast.LENGTH_SHORT).show();
                    loadAllData(); // OSVEŽI SVE LISTE
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    // Dodaj slične metode za loadActiveFriends() i loadAvailableUsers()...

    private void sendFriendRequest() {
        int selectedPos = spinnerAvailableUsers.getSelectedItemPosition();

        // Proveravamo da li je izabran pravi korisnik (pozicija 0 je "Izaberi korisnika...")
        if (selectedPos > 0) {
            User selectedUser = availableUsersList.get(selectedPos - 1);
            long receiverId = selectedUser.getId();

            apiService.sendRequest(currentUserId, receiverId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(FriendsActivity.this, "Zahtev poslat!", Toast.LENGTH_SHORT).show();
                        loadAllData(); // Osvežavamo sve liste da se vidi novi poslati zahtev
                    } else {
                        Toast.makeText(FriendsActivity.this, "Greška pri slanju", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("API_ERROR", "Slanje propalo: " + t.getMessage());
                }
            });
        } else {
            Toast.makeText(this, "Moraš izabrati korisnika iz liste", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadActiveFriends() {
        apiService.getActiveFriends(currentUserId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Friendship> activeList = new ArrayList<>();
                    for (User u : response.body()) {
                        Friendship f = new Friendship();
                        f.setUser1(u); // Za prikaz imena
                        activeList.add(f);
                    }
                    adapterActive = new FriendshipAdapter(activeList, "ACTIVE", (f, action) -> {
                        if (action.equals("REMOVE")) {
                            // Koristimo novu metodu koju smo kreirali u prethodnom koraku
                            removeFriendshipByUser(f.getUser1().getId());
                        }
                    });
                    rvActive.setAdapter(adapterActive);
                }
            }
            @Override public void onFailure(Call<List<User>> call, Throwable t) {}
        });
    }

    private void removeFriendshipByUser(long friendId) {
        Log.d("API_POZIV", "Uklanjam prijatelja: " + friendId + " za korisnika: " + currentUserId);

        // Pozivamo apiService metodu koja prima dva parametra (userId i friendId)
        apiService.removeActiveFriend(currentUserId, friendId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FriendsActivity.this, "Prijatelj uklonjen", Toast.LENGTH_SHORT).show();
                    loadAllData();
                } else {
                    Log.e("API_ERROR", "Kod greške: " + response.code());
                    Toast.makeText(FriendsActivity.this, "Greška: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("API_FAILURE", t.getMessage());
            }
        });
    }

    private void removeFriend(long friendshipId) {
        Log.d("API_POZIV", "Pokušavam brisanje zahteva sa ID: " + friendshipId);

        apiService.removeFriend(friendshipId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("API_SUCCESS", "Uspešno obrisano sa servera");
                    Toast.makeText(FriendsActivity.this, "Zahtev otkazan/obrisan", Toast.LENGTH_SHORT).show();
                    loadAllData(); // Ovo mora da osveži listu
                } else {
                    Log.e("API_ERROR", "Server vratio grešku: " + response.code());
                    Toast.makeText(FriendsActivity.this, "Greška servera: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("API_FAILURE", "Mreža ili Retrofit greška: " + t.getMessage());
                Toast.makeText(FriendsActivity.this, "Greška u komunikaciji", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rejectFriendRequest(long requestId) {
        apiService.rejectRequest(requestId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FriendsActivity.this, "Zahtev odbijen", Toast.LENGTH_SHORT).show();
                    loadAllData();
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}