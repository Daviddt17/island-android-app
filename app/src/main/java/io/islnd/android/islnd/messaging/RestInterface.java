package io.islnd.android.islnd.messaging;

import io.islnd.android.islnd.messaging.crypto.EncryptedData;
import io.islnd.android.islnd.messaging.crypto.EncryptedEvent;
import io.islnd.android.islnd.messaging.server.EventQuery;
import io.islnd.android.islnd.messaging.server.EventQueryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RestInterface {
    @GET("/readers/{username}")
    Call<List<EncryptedData>> readers(
            @Path("username") String username,
            @Query("apiKey") String apiKey);

    @POST("/publicKey/{username}")
    Call<String> postPublicKey(
            @Path("username") String username,
            @Body String publicKey,
            @Query("apiKey") String apiKey);


    @POST("/event")
    Call<Void> postEvent(
            @Body EncryptedEvent encryptedEvent,
            @Query("apiKey") String apiKey);

    @POST("/eventQuery")
    Call<EventQueryResponse> postEventQuery(
            @Body EventQuery eventQuery,
            @Query("apiKey") String apiKey);
}
