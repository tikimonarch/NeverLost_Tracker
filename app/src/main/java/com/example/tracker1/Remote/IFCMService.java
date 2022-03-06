package com.example.tracker1.Remote;

import com.example.tracker1.Model.MyResponse;
import com.example.tracker1.Model.Request;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
        "Content-Type:application/json",
            "Authorization:key=AAAAKz9FUbc:APA91bFWZfyor9ePVHLq2oK1HssWtGtPOeTEuwEsDJSSP3hhlmlMlb7YhqkmzYktadobcXOXgEU74D8F3wgNNosqYs8HfE0muljtXAoDL6QED3Cj4h0sCIBQTuyqem8HYKDk75ZVCM5d"
    })
    @POST("fcm/send")
    Observable<MyResponse> sendFriendRequestToUser(@Body Request body);
}
