package com.shienh.rajbir.smokescreen;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText mEmail, mName, mPassword, mConfirmPassword;
    private Button mRegisterEmail;
    private LinearLayout mBaseView;
    private int SNACK_DURATION = 3000;
    private ProgressDialog mProgress;
    private StorageReference mStorage;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomFont.replaceDefaultFont(this, "DEFAULT", "fonts/custom.ttf");
        CustomFont.replaceDefaultFont(this, "MONOSPACE", "fonts/custom.ttf");
        CustomFont.replaceDefaultFont(this, "SERIF", "fonts/custom.ttf");
        CustomFont.replaceDefaultFont(this, "SANS_SERIF", "fonts/custom.ttf");
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseReference = DatabasePersistence.getDatabase().getReference().child("Users");

        mBaseView = findViewById(R.id.registerLayout);

        mName = findViewById(R.id.xName);
        mEmail = findViewById(R.id.xRegisterEmail);
        mPassword = findViewById(R.id.xRegisterPassword);
        mConfirmPassword = findViewById(R.id.xRegisterConfirmPassword);

        mProgress = new ProgressDialog(this);

        mRegisterEmail = findViewById(R.id.xRegisterButton);
        mRegisterEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegister();
            }
        });

    }

    private void startRegister() {
        final String name = mName.getText().toString().trim();
        final String email = mEmail.getText().toString().trim();
        final String pass = mPassword.getText().toString().trim();
        String conPass = mConfirmPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(name)) {
            if (!TextUtils.isEmpty(email)) {
                if (!TextUtils.isEmpty(pass) && !TextUtils.isEmpty(conPass)) {
                    if (pass.equals(conPass)) {
                        mProgress.setMessage("Signing up....");
                        mProgress.show();
                        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                                    DatabaseReference current_user_db = mDatabaseReference.child(user_id);
                                    current_user_db.child("name").setValue(name);
                                    current_user_db.child("email").setValue(email);
                                    current_user_db.child("password").setValue(pass);
                                    mProgress.dismiss();
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            }
                        });
                    } else {
                        Snackbar.make(mBaseView, "Password and Confirm Password should be same...", SNACK_DURATION).show();
                    }
                } else {
                    Snackbar.make(mBaseView, "Password cannot be empty", SNACK_DURATION).show();
                }
            } else {
                Snackbar.make(mBaseView, "Email cannot be empty...", SNACK_DURATION).show();
            }
        } else {
            Snackbar.make(mBaseView, "Name cannot be Empty...", SNACK_DURATION).show();
        }
    }
}
