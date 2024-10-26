package dev.gustavoesposar.reminder;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("loginMobile") //Endpoint para login
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
}
