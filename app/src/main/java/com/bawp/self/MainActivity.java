package com.bawp.self;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tapadoo.alerter.Alerter;
import com.tapadoo.alerter.OnHideAlertListener;
import com.tapadoo.alerter.OnShowAlertListener;

import java.util.Objects;

import javax.annotation.Nullable;

import model.Journal;
import util.JournalApi;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private long backPressedTime;
    private Toast backToast;




    private FirebaseFirestore db =  FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button getStartedButton = findViewById(R.id.startButton);

        //check network state
        if(amIConnected())
        {


        }
        else
        {
            Toast.makeText(MainActivity.this, "No active network connection", Toast.LENGTH_SHORT).show();
        }




        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    currentUser = firebaseAuth.getCurrentUser();
                    final String currentUserId = currentUser.getUid();

                    collectionReference
                            .whereEqualTo("userId", currentUserId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                    @Nullable FirebaseFirestoreException e) {
                                    if (e != null) {
                                        return;
                                    }

                                    String name;

                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                            JournalApi journalApi = JournalApi.getInstance();
                                            journalApi.setUserId(snapshot.getString("userId"));
                                            journalApi.setUsername(snapshot.getString("username"));

                                            startActivity(new Intent(MainActivity.this,
                                                    JournalListActivity.class));
                                            finish();


                                        }
                                    }

                                }
                            });

                }else {

                }
            }
        };


        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(amIConnected())
                {


                }
                else
                {
                    showAlerter(v);
                    return;

                }





                //we go to LoginActivity
                startActivity(new Intent(MainActivity.this,
                        LoginActivity.class));
                finish();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
    private boolean amIConnected()
    {
        ConnectivityManager connectivityManager= (ConnectivityManager)getSystemService(MainActivity.this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo !=null && networkInfo.isConnected();

    }
    //used for showing notification if user has no internet connection
    public void showAlerter(View v) {
        Alerter.create(this)
                .setTitle("no network")
                .setText("Network not available , the App might not work")
                .setIcon(R.drawable.alerter_ic_notifications)
                .setBackgroundColorRes(R.color.centerGColor)
                .setDuration(5000)
                .enableSwipeToDismiss() //seems to not work well with OnClickListener
                .enableProgress(true)
                .setProgressColorRes(R.color.colorPrimary)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //do something when Alerter message was clicked
                    }
                })
                .setOnShowListener(new OnShowAlertListener() {
                    @Override
                    public void onShow() {
                        //do something when Alerter message shows
                    }
                })
                .setOnHideListener(new OnHideAlertListener() {
                    @Override
                    public void onHide() {
                        //do something when Alerter message hides
                    }
                })
                .show();
    }
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel();
            super.onBackPressed();
            return;
        } else {
            backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }

        backPressedTime = System.currentTimeMillis();
    }






}
