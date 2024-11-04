package dev.gustavoesposar.reminder.view;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.gustavoesposar.reminder.model.Aniversariante;
import dev.gustavoesposar.reminder.R;
import dev.gustavoesposar.reminder.network.ApiClient;
import dev.gustavoesposar.reminder.network.ApiService;
import dev.gustavoesposar.reminder.repository.AniversarianteRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AniversariosFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private AniversarianteRepository aniversarianteRepository;
    private ApiService apiService;
    private RecyclerView recyclerView;
    private AniversarianteAdapter aniversarianteAdapter;
    private final List<Aniversariante> aniversariantes = new ArrayList<>();
    private ProgressBar progressBarLoading;
    private ImageView imageViewEmpty;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aniversarianteRepository = new AniversarianteRepository(requireContext());
        setupApiService();
    }

    private void setupApiService() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aniversarios, container, false);
        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefreshLayout();
        return view;
    }

    private void initializeViews(View view) {
        progressBarLoading = view.findViewById(R.id.progressBarLoading);
        imageViewEmpty = view.findViewById(R.id.imageViewEmpty);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerViewAniversariantes);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        aniversarianteAdapter = new AniversarianteAdapter(aniversariantes, this::deletarAniversariante);
        recyclerView.setAdapter(aniversarianteAdapter);
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(this::carregarAniversariantesDoServidor);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        carregarAniversariantesDoServidor();
    }

    private void carregarAniversariantesLocalmente() {
        new Thread(() -> {
            aniversariantes.clear();
            aniversariantes.addAll(aniversarianteRepository.getAllAniversariantes());
            updateUiOnLocalLoad();
        }).start();
    }

    private void updateUiOnLocalLoad() {
        if (isAdded()) { // Verifica se o Fragment está anexado à Activity
            requireActivity().runOnUiThread(() -> {
                aniversarianteAdapter.notifyDataSetChanged();
                toggleEmptyImageVisibility();
            });
        }
    }

    private void carregarAniversariantesDoServidor() {
        String token = getTokenFromPreferences();
        if (token == null) {
            Toast.makeText(getContext(), "Token não encontrado. Faça login novamente.", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        showLoading();
        apiService.getBirthdates("Bearer " + token).enqueue(new Callback<List<Aniversariante>>() {
            @Override
            public void onResponse(Call<List<Aniversariante>> call, Response<List<Aniversariante>> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    updateLocalDatabase(response.body());
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Aniversariante>> call, Throwable t) {
                hideLoading();
                Toast.makeText(getContext(), "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void updateLocalDatabase(List<Aniversariante> newAniversariantes) {
        new Thread(() -> {
            aniversarianteRepository.deleteAll();
            aniversarianteRepository.insertAll(newAniversariantes);
            carregarAniversariantesLocalmente();
        }).start();
    }

    private String getTokenFromPreferences() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("JWT_TOKEN", null);
    }

    private void deletarAniversariante(Aniversariante aniversariante) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmação de Exclusão")
                .setMessage("Você realmente deseja excluir este aniversariante?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    realizarExclusao(aniversariante);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void realizarExclusao(Aniversariante aniversariante) {
        String token = getTokenFromPreferences();
        if (token == null) {
            Toast.makeText(getContext(), "Token de autenticação ausente", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("_id", String.valueOf(aniversariante.get_id()));

        apiService.deleteAniversariante("Bearer " + token, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    onDeleteSuccess(aniversariante);
                } else {
                    Toast.makeText(getContext(), "Erro ao deletar no servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Falha na conexão com o servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onDeleteSuccess(Aniversariante aniversariante) {
        Toast.makeText(getContext(), "Aniversariante deletado!", Toast.LENGTH_SHORT).show();
        aniversariantes.remove(aniversariante);
        aniversarianteAdapter.notifyDataSetChanged();
        carregarAniversariantesDoServidor();
    }

    private void toggleEmptyImageVisibility() {
        if (aniversariantes.isEmpty()) {
            showEmptyImage();
        } else {
            hideEmptyImage();
        }
    }

    private void showLoading() {
        progressBarLoading.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        progressBarLoading.setVisibility(View.GONE);
    }

    private void showEmptyImage() {
        imageViewEmpty.setVisibility(View.VISIBLE);
    }

    private void hideEmptyImage() {
        imageViewEmpty.setVisibility(View.GONE);
    }
}
