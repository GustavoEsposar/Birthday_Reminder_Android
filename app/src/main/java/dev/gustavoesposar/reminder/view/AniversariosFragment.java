package dev.gustavoesposar.reminder.view;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.gustavoesposar.reminder.model.Aniversariante;
import dev.gustavoesposar.reminder.database.AniversarianteDao;
import dev.gustavoesposar.reminder.R;
import dev.gustavoesposar.reminder.database.AppDatabase;
import dev.gustavoesposar.reminder.network.ApiClient;
import dev.gustavoesposar.reminder.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AniversariosFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private AniversarianteDao aniversarianteDao;
    private ApiService apiService;
    private RecyclerView recyclerView;
    private AniversarianteAdapter aniversarianteAdapter;
    private List<Aniversariante> aniversariantes = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppDatabase db = Room.databaseBuilder(requireContext(),
                AppDatabase.class, "AppDatabase").fallbackToDestructiveMigration().build();
        aniversarianteDao = db.aniversarianteDao();

        apiService = ApiClient.getClient().create(ApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aniversarios, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::carregarAniversariantesDoServidor);

        recyclerView = view.findViewById(R.id.recyclerViewAniversariantes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        aniversarianteAdapter = new AniversarianteAdapter(aniversariantes, this::deletarAniversariante);
        recyclerView.setAdapter(aniversarianteAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        carregarAniversariantesDoServidor();
    }

    private void carregarAniversariantesLocalmente() {
        new Thread(() -> {
            List<Aniversariante> localAniversariantes = aniversarianteDao.getAllAniversariantes();
            aniversariantes.clear();
            aniversariantes.addAll(localAniversariantes);
            requireActivity().runOnUiThread(() -> aniversarianteAdapter.notifyDataSetChanged());
        }).start();
    }

    private void carregarAniversariantesDoServidor() {
        if (!isAdded()) return;

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("JWT_TOKEN", null);

        if (token != null) {
            Call<List<Aniversariante>> call = apiService.getBirthdates("Bearer " + token);

            call.enqueue(new Callback<List<Aniversariante>>() {
                @Override
                public void onResponse(Call<List<Aniversariante>> call, Response<List<Aniversariante>> response) {
                    if (isAdded() && response.isSuccessful() && response.body() != null) {
                        new Thread(() -> {
                            aniversarianteDao.deleteAll();
                            aniversarianteDao.insertAll(response.body());

                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> carregarAniversariantesLocalmente());
                            }
                        }).start();
                    }

                    if (isAdded()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void onFailure(Call<List<Aniversariante>> call, Throwable t) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        } else {
            if (isAdded()) {
                Toast.makeText(getContext(), "Token não encontrado. Faça login novamente.", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void deletarAniversariante(Aniversariante aniversariante) {
        // Recupere o token do SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("JWT_TOKEN", null);

        if (token != null) {
            ApiService apiService = ApiClient.getClient().create(ApiService.class);

            Map<String, String> body = new HashMap<>();
            body.put("_id", String.valueOf(aniversariante.get_id()));

            Call<Void> call = apiService.deleteAniversariante("Bearer " + token, body);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Aniversariante deletado!", Toast.LENGTH_SHORT).show();
                        aniversariantes.remove(aniversariante);
                        aniversarianteAdapter.notifyDataSetChanged();
                        carregarAniversariantesDoServidor();
                    } else {
                        Toast.makeText(getContext(), "Erro ao deletar no servidor", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Falha na conexão com o servidor", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Token de autenticação ausente", Toast.LENGTH_SHORT).show();
        }
    }

}
