package com.shienh.rajbir.smokescreen;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class UserProfile extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private EditText mCigPerDay, mCigPerPack, mYearsOfSmoking, mPricePerPack;
    private Spinner mCurrencyType;
    private String currency = null;
    private LinearLayout mBaseView;

    private Button mSaveData, mDateTimePicker;

    private int mDay, mMonth, mYear, mHour, mMinute;
    private int mDayFinal, mMonthFinal, mYearFinal, mHourFinal, mMinuteFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mBaseView = findViewById(R.id.xUserLayout);

        mCigPerDay = findViewById(R.id.xCigPerDay);
        mCigPerPack = findViewById(R.id.xCigPerPack);
        mYearsOfSmoking = findViewById(R.id.xYearsOfSmoking);
        mPricePerPack = findViewById(R.id.xPricePerPack);

        mCurrencyType = findViewById(R.id.xUserCurrency);
        mCurrencyType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currency = String.valueOf(mCurrencyType.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mDateTimePicker = findViewById(R.id.xUserDateTimePicker);
        mDateTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDateTime();
            }
        });

        mSaveData = findViewById(R.id.xUserSave);
        mSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempcpd = mCigPerDay.getText().toString();
                String tempcpp = mCigPerPack.getText().toString();
                String tempyos = mYearsOfSmoking.getText().toString();
                String tempppp = mPricePerPack.getText().toString();
                if (TextUtils.isEmpty(tempcpd)) {
                    Snackbar.make(mBaseView, "Cigarettes per day cannot be zero", 3000).show();
                } else if (TextUtils.isEmpty(tempcpp)) {
                    Snackbar.make(mBaseView, "Cigarettes per pack cannot be zero", 3000).show();
                } else if (TextUtils.isEmpty(tempyos)) {
                    Snackbar.make(mBaseView, "Years of smoking cannot be zero", 3000).show();
                } else if (TextUtils.isEmpty(tempppp)) {
                    Snackbar.make(mBaseView, "Price per pack cannot be zero", 3000).show();
                } else {
                    int cpd = Integer.parseInt(tempcpd);
                    int cpp = Integer.parseInt(tempcpp);
                    Float yos = Float.parseFloat(tempyos);
                    Float ppp = Float.parseFloat(tempppp);
                    String fCurrency = currency;
                    // Long tsLong = System.currentTimeMillis()/1000;
                    if ((cpd > 0) && (cpp > 0) && (yos > 0) && (ppp > 0) && (fCurrency != null) &&
                            (mYearFinal > 0) && (mMonthFinal > 0) && (mDayFinal > 0)) {
                        SharedPreferences sharedPreferences = getSharedPreferences("userdata", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("CPD", cpd);
                        editor.putInt("CPP", cpp);
                        editor.putFloat("YOS", yos);
                        editor.putFloat("PPP", ppp);
                        editor.putString("CUR", fCurrency);
                        editor.putInt("YEAR", mYearFinal);
                        editor.putInt("MONTH", mMonthFinal);
                        editor.putInt("DAY", mDayFinal);
                        editor.putInt("HOUR", mHourFinal);
                        editor.putInt("MINUTE", mMinuteFinal);
                        editor.apply();
                        Intent loginIntent = new Intent(UserProfile.this, MainActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginIntent);
                        Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(mBaseView, "Make sure all values are correct, not Zero or negative", 3000).show();
                    }
                }
            }
        });
    }

    private void pickDateTime() {
        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(UserProfile.this, UserProfile.this, mYear, mMonth, mDay);
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
        TimePickerDialog timePickerDialog = new TimePickerDialog(UserProfile.this,
                UserProfile.this, mHour, mMinute, DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        mHourFinal = hourOfDay;
        mMinuteFinal = minute;
        Calendar calendar = new GregorianCalendar(mYearFinal, mMonthFinal, mDayFinal, mHourFinal, mMinuteFinal);
        long finalTimeStamp = calendar.getTimeInMillis() / 1000;
        SharedPreferences sharedPreferences = getSharedPreferences("userdata", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("TIMESTAMP", finalTimeStamp);
        //  Toast.makeText(getApplicationContext(),String.valueOf(finalTimeStamp),Toast.LENGTH_LONG).show();
        editor.apply();
 /*       Toast.makeText(getApplicationContext(), "Year: " + mYearFinal + " \n" +
                "Month: " + mMonthFinal + " \n" +
                "Day: " + mDayFinal + " \n" +
                "Hour: " + mHourFinal + " \n" +
                "Minute: " + mMinuteFinal, Toast.LENGTH_LONG).show();*/
    }
}
