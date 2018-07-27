package com.shienh.rajbir.smokescreen;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "MAIN_ACTIVITY";
    protected int mProgressStatus = 0;
    private RelativeLayout mRelative;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mDatabase;
    private int defaultValue = 0;
    private int CPD = 0, CPP = 0;
    private float YOS = 0, PPP = 0;
    private String CUR = null;
    private TextView mTextProgress, mDayText, mSmokeFreeSince, mMoneySaved, mNumberofDays, mLifeRegained, mCigNotSmoked, mTotalCigSmoked, mMoneyWasted, mLifeLost;
    private CircularProgressBar mProgressBar;
    private Handler handler = new Handler();

    private int mDay, mMonth, mYear, mHour, mMinute;
    private int mDayFinal = 0, mMonthFinal = 0, mYearFinal = 0, mHourFinal = 0, mMinuteFinal = 0;
    private SharedPreferences sharedPreferences;

    private Long mTimeStamp;
    private int dayNumber = 1;

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

        mNumberofDays = findViewById(R.id.xNumberOfDays);

        mTextProgress = findViewById(R.id.txtProgress);
        mDayText = findViewById(R.id.xDay);
        mSmokeFreeSince = findViewById(R.id.xSmokeFreeSince);
        mMoneySaved = findViewById(R.id.xMoneySaved);
        mLifeRegained = findViewById(R.id.xLifeRegained);
        mCigNotSmoked = findViewById(R.id.xCigNotSmoked);
        mTotalCigSmoked = findViewById(R.id.xTotalCigSmoked);
        mMoneyWasted = findViewById(R.id.xMoneyWasted);
        mLifeLost = findViewById(R.id.xLifeLost);

        mProgressBar = findViewById(R.id.xMainProgressBar);
        mProgressBar.setColor(ContextCompat.getColor(this, R.color.colorAccent));
        mProgressBar.setBackgroundColor(ContextCompat.getColor(this, R.color.com_facebook_blue));
        mProgressBar.setProgressBarWidth(getResources().getDimension(R.dimen.button_margin));
        mProgressBar.setBackgroundProgressBarWidth(getResources().getDimension(R.dimen.button_margin));
        int animationDuration = 2500; // 2500ms = 2,5s
        mProgressBar.setProgressWithAnimation(1, animationDuration); // Default duration = 1500ms

        Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        int mSeconds = c.get(Calendar.SECOND);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        final int fixedTimeFromUSer = mDayFinal + (mHourFinal / 24) + (mMinuteFinal / (24 * 60));
        final int tempTime = fixedTimeFromUSer + 1;
        final int fCPD = CPD;
        final float fYOS = YOS;
        final float fPPP = PPP;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mProgressStatus <= 100) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            int currentTimeFromUser = mDay + (mHour / 24) + (mMinute / (24 * 60));
                            mProgressStatus = (fixedTimeFromUSer / tempTime) * 100;
                            mProgressBar.setProgress(mProgressStatus);
                            mTextProgress.setText(mProgressStatus + " %");

                            mSmokeFreeSince.setText((mDay - mDayFinal) + "d " + (mHour - mHourFinal) + "h " + (mMinute - mMinuteFinal) + "m");
                            mMoneySaved.setText("$" + ((mDay - mDayFinal) * (int) fPPP));
                            mLifeRegained.setText((mMinute - mMinuteFinal) * 275 + " mins");
                            mCigNotSmoked.setText(String.valueOf(fCPD * (mDay - mDayFinal)));
                            mTotalCigSmoked.setText(String.valueOf((int) (fCPD * fYOS * 365)) + "\n Cigarettes Smoked");
                            mMoneyWasted.setText("$" + String.valueOf((int) (fPPP * (fYOS) * 365)) + "\n Money Wasted");
                            mLifeLost.setText(String.valueOf(275 * fYOS) + " months \n Life Lost");
                        }
                    });
                    try {
                        Thread.sleep(1);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mTimeStamp++;
                    if (mProgressStatus == 100) {
                        dayNumber++;
                        mDayText.setText("Day :" + dayNumber);
                        mNumberofDays.setText("It's day number :" + dayNumber);
                    }
                }
            }
        }).start();

        secondGridManager();

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

    private void secondGridManager() {
        Date date = new Date(Long.parseLong(mTimeStamp.toString()));
        mSmokeFreeSince.setText(dayNumber + "d");
    }

    private void loadUserProfile() {
        sharedPreferences = getApplicationContext().getSharedPreferences("userdata", MODE_PRIVATE);
        CPD = sharedPreferences.getInt("CPD", defaultValue);
        CPP = sharedPreferences.getInt("CPP", defaultValue);
        YOS = sharedPreferences.getFloat("YOS", defaultValue);
        PPP = sharedPreferences.getFloat("PPP", defaultValue);
        CUR = sharedPreferences.getString("CUR", null);
        mYearFinal = sharedPreferences.getInt("YEAR", defaultValue);
        mMonthFinal = sharedPreferences.getInt("MONTH", defaultValue);
        mDayFinal = sharedPreferences.getInt("DAY", defaultValue);
        mHourFinal = sharedPreferences.getInt("HOUR", defaultValue);
        mMinuteFinal = sharedPreferences.getInt("MINUTE", defaultValue);
        mTimeStamp = sharedPreferences.getLong("TIMESTAMP", defaultValue);
        if (CPD == defaultValue) {
            startActivity(new Intent(MainActivity.this, UserProfile.class));
        }
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
                pickDateTime();
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

    private void pickDateTime() {
        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, MainActivity.this, mYear, mMonth, mDay);
        datePickerDialog.show();
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        mYearFinal = year;
        mMonthFinal = month;
        mDayFinal = dayOfMonth;

        Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                MainActivity.this, mHour, mMinute, DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mHourFinal = hourOfDay;
        mMinuteFinal = minute;
      /*  Toast.makeText(getApplicationContext(), "Year: " + mYearFinal + " \n" +
                "Month: " + mMonthFinal + " \n" +
                "Day: " + mDayFinal + " \n" +
                "Hour: " + mHourFinal + " \n" +
                "Minute: " + mMinuteFinal, Toast.LENGTH_LONG).show();*/
    }

    private void logout() {
        FirebaseUser user = mAuth.getCurrentUser();
        do {
            sharedPreferences.edit().clear().apply();
            FirebaseAuth.getInstance().signOut();
            mAuth.signOut();
        } while (user == null);
        finish();
    }

//    public class CustomCountDownTimer extends CountDownTimer{
//
//        /**
//         * @param millisInFuture    The number of millis in the future from the call
//         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
//         *                          is called.
//         * @param countDownInterval The interval along the way to receive
//         *                          {@link #onTick(long)} callbacks.
//         */
//        public CustomCountDownTimer(long millisInFuture, long countDownInterval) {
//            super(millisInFuture, countDownInterval);
//        }
//
//        @Override
//        public void onTick(long millisUntilFinished) {
//            mProgressStatus = (int)(millisUntilFinished/100);
//            mProgressBar.setProgress(mProgressStatus);
//            mTextProgress.setText(mProgressStatus + " %");
//        }
//
//        @Override
//        public void onFinish() {
//        //give notification of greetings
//        }
//    }
}
