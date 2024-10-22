package dev.gustavoesposar.reminder;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Configurar comportamento de clique nos itens da navbar
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                // Usar if-else em vez de switch
                int id = item.getItemId();

                if (id == R.id.nav_aniversarios) {
                    selectedFragment = new AniversariosFragment();
                } else if (id == R.id.nav_novoaniversario) {
                    selectedFragment = new NovoAniversarianteFragment();
                } else if (id == R.id.nav_qrcode) {
                    selectedFragment = new QrcodeFragment();
                }

                // Trocar fragmento quando item for selecionado
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFragment).commit();
                }

                return true;
            }
        });


        // Definir a tela inicial ao iniciar
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new AniversariosFragment()).commit();
        }
    }
}