package dev.gustavoesposar.reminder.network;

import java.util.List;
import java.util.Map;

import dev.gustavoesposar.reminder.model.Aniversariante;
import dev.gustavoesposar.reminder.model.LoginRequest;
import dev.gustavoesposar.reminder.model.LoginResponse;
import dev.gustavoesposar.reminder.model.RegisterRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("loginMobile")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("getBirthdates")
    Call<List<Aniversariante>> getBirthdates(@Header("Authorization") String token);

    @POST("/deleteBirthdateMobile")
    Call<Void> deleteAniversariante(@Header("Authorization") String token, @Body Map<String, String> body);

    @POST("/addBirthdateMobile")
    Call<Void> addAniversariante(@Header("Authorization") String token, @Body Map<String, String> body);

    @POST("/registerMobile")
    Call<Void> cadastrarUsuario(@Body RegisterRequest cadastroRequest);

    @POST("/validateToken")
    Call<Void> validateToken(@Header("Authorization") String token);
}
