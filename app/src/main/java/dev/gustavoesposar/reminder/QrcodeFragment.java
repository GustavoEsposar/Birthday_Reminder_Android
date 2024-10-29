package dev.gustavoesposar.reminder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qrcode, container, false);

        ImageView qrCodeImageView = view.findViewById(R.id.qrCode);
        ImageButton openCameraButton = view.findViewById(R.id.openCamera);

        File qrCodeFile = new File(requireContext().getFilesDir(), "qr_code.png");
        if (qrCodeFile.exists()) {
            Bitmap qrBitmap = BitmapFactory.decodeFile(qrCodeFile.getAbsolutePath());
            qrCodeImageView.setImageBitmap(qrBitmap);
        }

        // Inicializa o launcher para solicitar a permissão de câmera
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        // Permissão negada; mostre uma mensagem informando que a câmera é necessária
                    }
                }
        );

        openCameraButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                // Solicita a permissão
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        return view;
    }

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    processImageFromCamera(result.getData());
                }
            }
    );

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void processImageFromCamera(Intent data) {
        Bundle extras = data.getExtras();
        Bitmap bitmap = (Bitmap) extras.get("data");

        if (bitmap != null) {
            InputImage image = InputImage.fromBitmap(bitmap, 0);

            BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build();

            BarcodeScanner scanner = BarcodeScanning.getClient(options);

            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String qrData = barcode.getRawValue();
                            if (qrData != null) {
                                handleQRCodeData(qrData);
                            }
                        }
                    })
                    .addOnFailureListener(e -> e.printStackTrace());
        }
    }

    private void handleQRCodeData(String qrData) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("JWT_TOKEN", null);

        if (token != null) {
            JSONObject qrDataJson = null;
            try {
                qrDataJson = new JSONObject(qrData);
                String name = qrDataJson.getString("name");
                String birthdate = qrDataJson.getString("birthdate");

                String[] dateParts = birthdate.split("/");
                String day = dateParts[0].length() == 1 ? "0" + dateParts[0] : dateParts[0];
                String month = dateParts[1].length() == 1 ? "0" + dateParts[1] : dateParts[1];
                String year = dateParts[2];
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
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Erro ao processar o QR Code", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Token de autenticação ausente", Toast.LENGTH_SHORT).show();
        }
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
}
