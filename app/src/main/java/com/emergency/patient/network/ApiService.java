package com.emergency.patient.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import java.util.Map;

/**
 * ApiService — Retrofit interface for REST endpoints.
 */
public interface ApiService {

    /**
     * Uploads the FCM device token to the backend for push notifications.
     */
    @POST("patient/update-fcm-token")
    Call<Void> uploadFcmToken(@Body Map<String, String> body);

    /**
     * Placeholder for profile and other updates.
     */
}
