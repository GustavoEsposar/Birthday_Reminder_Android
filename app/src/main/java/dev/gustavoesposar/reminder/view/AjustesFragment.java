package dev.gustavoesposar.reminder.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

import dev.gustavoesposar.reminder.R;

public class AjustesFragment extends Fragment {
    private TextView textViewLogout;
    private TextView textViewDeveloper;
    private final String URL_PORTIFOLIO = "http://gustavoesposar.dev";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ajustes, container, false);

        textViewLogout = view.findViewById(R.id.textViewLogout);
        textViewLogout.setOnClickListener(v -> fazerLogout());

        textViewDeveloper = view.findViewById(R.id.developer_text);;
        textViewDeveloper.setOnClickListener(v -> redirecionarParaPortifolio());

        return view;
    }

    private void fazerLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirmação de Logout")
                .setMessage("Tem certeza de que deseja sair?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
                    sharedPreferences.edit().clear().apply();

                    Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void redirecionarParaPortifolio() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(android.net.Uri.parse(URL_PORTIFOLIO));
        startActivity(intent);
    }
}