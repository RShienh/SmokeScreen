package com.shienh.rajbir.smokescreen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class UserProfile extends AppCompatActivity {

    private EditText mCigPerDay, mCigPerPack, mYearsOfSmoking, mPricePerPack;
    private Spinner mCurrencyType;
    private String currency = null;
    private LinearLayout mBaseView;

    private Button mSaveData;

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
                    if ((cpd > 0) && (cpp > 0) && (yos > 0) && (ppp > 0) && (fCurrency != null)) {
                        SharedPreferences sharedPreferences = getSharedPreferences("userdata", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("CPD", cpd);
                        editor.putInt("CPP", cpp);
                        editor.putFloat("YOS", yos);
                        editor.putFloat("PPP", ppp);
                        editor.putString("CUR", fCurrency);
                        editor.apply();
                    } else {
                        Snackbar.make(mBaseView, "Make sure all values are correct, not Zero or negative", 3000).show();
                    }
                }
            }
        });
    }

}
