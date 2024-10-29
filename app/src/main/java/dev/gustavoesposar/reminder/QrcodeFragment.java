package dev.gustavoesposar.reminder;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrcodeFragment extends Fragment {

    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ImageButton openCameraButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qrcode, container, false);

        ImageView qrCodeImageView = view.findViewById(R.id.qrCode);
        openCameraButton = view.findViewById(R.id.openCamera);

        File qrCodeFile = new File(requireContext().getFilesDir(), "qr_code.png");
        if (qrCodeFile.exists()) {
            Bitmap qrBitmap = BitmapFactory.decodeFile(qrCodeFile.getAbsolutePath());
            qrCodeImageView.setImageBitmap(qrBitmap);
        }

        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        scanCode();
                    } else {
                        Toast.makeText(requireContext(), "Permissão necessária para esta operação", Toast.LENGTH_LONG).show();
                    }
                }
        );

        openCameraButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                scanCode();
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        return view;
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Aproxime o QR Code");
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        cameraLauncher.launch(options);
    }

    private final ActivityResultLauncher<ScanOptions> cameraLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            Log.d("QRCode", "Scanned: " + result.getContents());
            handleQRCodeData(result.getContents());
        }
    });

    private void handleQRCodeData(String qrData) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("JWT_TOKEN", null);

        if (token != null) {
            try {
                JSONObject qrDataJson = null;
                qrDataJson = new JSONObject(qrData);
                if(qrDataJson.has("name") && qrDataJson.has("birthdate")) {
                    String name = qrDataJson.getString("name");
                    String birthdate = qrDataJson.getString("birthdate");

                    String year = birthdate.substring(0, 4);
                    String month = birthdate.substring(5, 7);
                    String day = birthdate.substring(8, 10);
                    String formattedDate = year + "-" + month + "-" + day;

                    ApiService apiService = ApiClient.getClient().create(ApiService.class);

                    Map<String, String> body = new HashMap<>();
                    body.put("name", name);
                    body.put("date", formattedDate);

                    Call<Void> call = apiService.addAniversariante("Bearer " + token, body);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), name.split(" ")[0] + " foi adicionado!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Erro ao adicionar no servidor", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(getContext(), "Falha na conexão com o servidor", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Erro ao processar o QR Code", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Token de autenticação ausente", Toast.LENGTH_SHORT).show();
        }
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
                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                outputStream.close();

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
