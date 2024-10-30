package dev.gustavoesposar.reminder;

import java.util.List;
import java.util.Map;

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
}
