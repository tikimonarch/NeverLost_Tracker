package com.example.tracker1;

import static com.firebase.ui.auth.AuthUI.*;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.tracker1.Model.User;
import com.example.tracker1.Util.AlarmReceiver;
import com.firebase.ui.auth.AuthUI;
import com.example.tracker1.Util.Common;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    DatabaseReference user_information;
    //private static final int MY_REQUEST_CODE = 3102; //custom number
    List<AuthUI.IdpConfig> providers;
    //private static final String TAG = "DebugLogIn";

    private ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult activityResult) {
                            int resultCode = activityResult.getResultCode();
                            Intent data = activityResult.getData();
                            IdpResponse response = IdpResponse.fromResultIntent(data);
                            if (resultCode == RESULT_OK)
                            {
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                //Check if user exists on Database
                                user_information.orderByKey()
                                        .equalTo(firebaseUser.getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.getValue() == null){//If User does not exist
                                                    if(!snapshot.child(firebaseUser.getUid()).exists()) //if key uid does not exist
                                                    {
                                                        Common.loggedUser= new User(firebaseUser.getUid(),firebaseUser.getEmail());
                                                        //Add to database
                                                        user_information.child(Common.loggedUser.getUid())
                                                                .setValue(Common.loggedUser);
                                                    }
                                                }
                                                else //if user is available
                                                {
                                                    Common.loggedUser = snapshot.child(firebaseUser.getUid()).getValue(User.class);
                                                }

                                                //Save Uid to storage to update location from background
                                                Paper.book().write(Common.USER_UID_SAVE_KEY,Common.loggedUser.getUid());
                                                updateToken(firebaseUser);
                                                setupUI();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
//                            else {
//                                // Sign in failed
//                                if (response == null) {
//                                    // User pressed back button
//                                    showSnackbar("Sign in cancelled.");
//                                    return;
//                                }
//
//                                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
//                                    showSnackbar("No internet connection.");
//                                    return;
//                                }
//
//                                showSnackbar("Unknown error.");
//                                Log.e(TAG, "Sign-in error: ", response.getError());
//                            }

                        }
                    }

            );

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        Paper.init(this);

//        createChannel();
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 14);
//        calendar.set(Calendar.MINUTE, 54);
//        calendar.set(Calendar.SECOND, 0);
//        Intent intent1 = new Intent(MainActivity.this, AlarmReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0,intent1, 0);
//        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
//        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        //Init firebase
        //rebuild if reference is not correct, check google-service
        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
        //Init providers
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        //Request permission of location
        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        startSignInOptions(); //original
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(MainActivity.this, "You must accept the permission to use the app", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
    }
    //Original
    private void startSignInOptions() {
        //TAKE NOTE!!!!!!!!!
        activityResultLauncher.launch(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        //.setIsSmartLockEnabled(!BuildConfig.DEBUG /* credentials */, true /* hints */)
                        .build());
        /*startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(), MY_REQUEST_CODE);*/
    }



/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK)
            {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                //Check if user exists on Database
                user_information.orderByKey()
                        .equalTo(firebaseUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() == null){
                                    if(!snapshot.child(firebaseUser.getUid()).exists())
                                    {
                                        Common.loggedUser= new User(firebaseUser.getUid(),firebaseUser.getEmail());
                                        //Add to database
                                        user_information.child(Common.loggedUser.getUid())
                                                .setValue(Common.loggedUser);
                                    }
                                }
                                else
                                {
                                    Common.loggedUser = snapshot.child(firebaseUser.getUid()).getValue(User.class);
                                }

                                //Save Uid to storage to update location from background
                                Paper.book().write(Common.USER_UID_SAVE_KEY,Common.loggedUser.getUid());
                                updateToken(firebaseUser);
                                setupUI();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        }
    }*/

    private void setupUI() {
        //Nav Home
        startActivity(new Intent(MainActivity.this,HomeActivity.class));
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        CharSequence name = "goHomeChannel";
        String description = "Channel for go home reminder";
        NotificationChannel trckrChannel = new NotificationChannel("notifyHome",
               name, NotificationManager.IMPORTANCE_DEFAULT);
        trckrChannel.setDescription(description);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(trckrChannel);
    }

    private void updateToken(final FirebaseUser firebaseUser) {
        final DatabaseReference tokens = FirebaseDatabase.getInstance()
                .getReference(Common.TOKENS);

        //get Token (New)
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                tokens.child(firebaseUser.getUid())
                        .setValue(s);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}