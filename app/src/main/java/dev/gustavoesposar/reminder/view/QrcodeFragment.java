package dev.gustavoesposar.reminder.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.Manifest;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import dev.gustavoesposar.reminder.utils.CadastroValidator;
import dev.gustavoesposar.reminder.utils.CaptureAct;
import dev.gustavoesposar.reminder.R;
import dev.gustavoesposar.reminder.network.ApiClient;
import dev.gustavoesposar.reminder.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrcodeFragment extends Fragment {

    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private ImageButton openCameraButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qrcode, container, false);
        initializeViews(view);
        initializeServices();
        setupCameraPermissionLauncher();
        loadQrCodeImage();

        openCameraButton.setOnClickListener(v -> handleCameraButtonClick());

        return view;
    }

    private void initializeViews(View view) {
        ImageView qrCodeImageView = view.findViewById(R.id.qrCode);
        openCameraButton = view.findViewById(R.id.openCamera);
    }

    private void initializeServices() {
        apiService = ApiClient.getClient().create(ApiService.class);
        sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
    }

    private void setupCameraPermissionLauncher() {
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        scanCode();
                    } else {
                        showToast("Permissão necessária para esta operação");
                    }
                }
        );
    }

    private void loadQrCodeImage() {
        ImageView qrCodeImageView = requireView().findViewById(R.id.qrCode);
        File qrCodeFile = new File(requireContext().getFilesDir(), "qr_code.png");
        if (qrCodeFile.exists()) {
            Bitmap qrBitmap = BitmapFactory.decodeFile(qrCodeFile.getAbsolutePath());
            qrCodeImageView.setImageBitmap(qrBitmap);
        }
    }

    private void handleCameraButtonClick() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            scanCode();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions()
                .setPrompt("Aproxime o QR Code")
                .setBeepEnabled(false)
                .setOrientationLocked(true)
                .setCaptureActivity(CaptureAct.class);
        cameraLauncher.launch(options);
    }

    private final ActivityResultLauncher<ScanOptions> cameraLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            Log.d("QRCode", "Scanned: " + result.getContents());
            handleQRCodeData(result.getContents());
        }
    });

    private void handleQRCodeData(String qrData) {
        String token = sharedPreferences.getString("JWT_TOKEN", null);
        if (token == null) {
            showToast("Token de autenticação ausente");
            return;
        }

        try {
            JSONObject qrDataJson = new JSONObject(qrData);
            if (qrDataJson.has("name") && qrDataJson.has("birthdate")) {
                String name = qrDataJson.getString("name");
                String formattedDate = CadastroValidator.formatBirthdate(qrDataJson.getString("birthdate"));
                sendDataToServer(token, name, formattedDate);
            }
        } catch (JSONException e) {
            showToast("Erro ao processar o QR Code");
        }
    }

    private void sendDataToServer(String token, String name, String formattedDate) {
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("date", formattedDate);

        Call<Void> call = apiService.addAniversariante("Bearer " + token, body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast(name.split(" ")[0] + " foi adicionado!");
                } else {
                    showToast("Erro ao adicionar no servidor");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Falha na conexão com o servidor");
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static void saveQrCodeImageLocally(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String qrCodeUrl = sharedPreferences.getString("QRCODE_URL", null);

        if (qrCodeUrl == null) {
            return;
        }

        new Thread(() -> {
            try {
                // Fazer o download da imagem
                URL url = new URL(qrCodeUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                // Salvar a imagem localmente como PNG
                File file = new File(context.getFilesDir(), "qr_code.png");
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static String getUrl(String name, String birth) {
        try {
            String data = URLEncoder.encode("{\"name\":\"" + name + "\",\"birthdate\":\"" + birth + "\"}", "UTF-8");
            return "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=" + data;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}