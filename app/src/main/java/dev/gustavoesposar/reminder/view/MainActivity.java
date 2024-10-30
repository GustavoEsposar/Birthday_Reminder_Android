package dev.gustavoesposar.reminder.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

import dev.gustavoesposar.reminder.R;
import dev.gustavoesposar.reminder.network.ApiClient;
import dev.gustavoesposar.reminder.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {

    private Map<Integer, Fragment> fragmentMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkTokenValidity();
        initializeFragmentMap();
        initializeBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new AniversariosFragment());
        }
    }

    private void checkTokenValidity() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("JWT_TOKEN", null);

        if (token == null) {
            redirectToLogin();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.validateToken("Bearer " + token);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                if (!response.isSuccessful()) {
                    redirectToLogin();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Falha na conexão com o servidor", Toast.LENGTH_SHORT).show();
                redirectToLogin();
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializeFragmentMap() {
        fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.nav_aniversarios, new AniversariosFragment());
        fragmentMap.put(R.id.nav_novoaniversario, new NovoAniversarianteFragment());
        fragmentMap.put(R.id.nav_qrcode, new QrcodeFragment());
    }

    private void initializeBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = fragmentMap.get(item.getItemId());
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }
}
