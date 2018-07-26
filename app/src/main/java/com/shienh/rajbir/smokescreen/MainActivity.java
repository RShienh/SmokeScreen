package com.shienh.rajbir.smokescreen;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout mRelative;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mDatabase;
    private int defaultValue = 0;
    private int CPD = 0, CPP = 0;
    private float YOS = 0, PPP = 0;
    private String CUR = null;

    private TextView mTextProgress;
    private ProgressBar mProgressBar;
    private int ProgressStatus = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomFont.replaceDefaultFont(this, "DEFAULT", "fonts/custom.ttf");
        CustomFont.replaceDefaultFont(this, "MONOSPACE", "fonts/custom.ttf");
        CustomFont.replaceDefaultFont(this, "SERIF", "fonts/custom.ttf");
        CustomFont.replaceDefaultFont(this, "SANS_SERIF", "fonts/custom.ttf");
        setContentView(R.layout.activity_main);

        loadUserProfile();
        mRelative = findViewById(R.id.MainRelative);

        mTextProgress = findViewById(R.id.txtProgress);
        mProgressBar = findViewById(R.id.xMainProgressBar);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (ProgressStatus <= 100) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setProgress(ProgressStatus);
                            mTextProgress.setText(ProgressStatus + " %");
                        }
                    });
                    try {
                        Thread.sleep(100);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ProgressStatus++;
                }
            }
        }).start();

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        mDatabase = DatabasePersistence.getDatabase().getReference().child("Users");
        mDatabase.keepSynced(true);
    }

    private void loadUserProfile() {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("userdata", MODE_PRIVATE);
        CPD = sharedPreferences.getInt("CPD", defaultValue);
        CPP = sharedPreferences.getInt("CPP", defaultValue);
        YOS = sharedPreferences.getFloat("YOS", defaultValue);
        PPP = sharedPreferences.getFloat("PPP", defaultValue);
        CUR = sharedPreferences.getString("CUR", null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserExists();
        mAuth.addAuthStateListener(mAuthStateListener);

    }

    private void checkUserExists() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            final String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(user_id)) {
                        Snackbar.make(mRelative, "Welcome Back!", 3000).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_start_over) {
            startOver();
        } else if (item.getItemId() == R.id.action_user_profile) {
            startActivity(new Intent(MainActivity.this, UserProfile.class));
        } else if (item.getItemId() == R.id.action_logout) {
            logout();
        } else if (item.getItemId() == R.id.action_exit) {
            exitPrompt();
        }
        return super.onOptionsItemSelected(item);
    }

    private void exitPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you want to exit ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void startOver() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Do you really want to start from scratch ?");
        builder.setPositiveButton("Yes, start over...", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("No, I can cope", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }


    private void logout() {
        FirebaseUser user = mAuth.getCurrentUser();
        do {
            FirebaseAuth.getInstance().signOut();
            mAuth.signOut();
        } while (user == null);
        finish();
    }
}
