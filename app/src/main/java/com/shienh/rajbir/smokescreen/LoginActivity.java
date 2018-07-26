package com.shienh.rajbir.smokescreen;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LOGIN_ACTIVITY";
    private FirebaseAuth mAuth;
    private EditText mLoginEmail, mLoginPass;
    private Button mLoginBTN, mFacebookButton;
    private TextView mRegister;
    private LinearLayout mBaseView;
    private int SNACK_DURATION = 3000;
    private DatabaseReference mDataBaseUser;
    private ProgressDialog mDialog;
    private SignInButton mGoogleButton;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomFont.replaceDefaultFont(this, "DEFAULT", "fonts/custom.ttf");
        CustomFont.replaceDefaultFont(this, "MONOSPACE", "fonts/custom.ttf");
        CustomFont.replaceDefaultFont(this, "SERIF", "fonts/custom.ttf");
        CustomFont.replaceDefaultFont(this, "SANS_SERIF", "fonts/custom.ttf");
        setContentView(R.layout.activity_login);

        //  keyHashGenerator();

        mAuth = FirebaseAuth.getInstance();

        mDataBaseUser = DatabasePersistence.getDatabase().getReference().child("Users");
        mDataBaseUser.keepSynced(true);

        mGoogleButton = findViewById(R.id.xLoginGoogle);

        mBaseView = findViewById(R.id.xLoginBaseView);
        mDialog = new ProgressDialog(this);
        mLoginEmail = findViewById(R.id.xLoginEmail);
        mLoginPass = findViewById(R.id.xLoginPassword);

        mRegister = findViewById(R.id.xLoginRegister);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        mLoginBTN = findViewById(R.id.xLoginButton);
        mLoginBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });

        //Google Sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        mCallbackManager = CallbackManager.Factory.create();

        mFacebookButton = findViewById(R.id.xLoginFacebook);
        mFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                        Snackbar.make(mBaseView, "Facebook Login Cancelled", 3000).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "facebook:onError", error);
                        Snackbar.make(mBaseView, "Facebook Login had an Error", 3000).show();
                        // ...
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            updateUI();
        }
    }

    private void updateUI() {
        Snackbar.make(mBaseView, "You are logged in!", SNACK_DURATION).show();
        Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI();
                        }

                        // ...
                    }
                });
    }


    /**
     * private void keyHashGenerator() {
     * // Add code to print out the key hash
     * try {
     * PackageInfo info = getPackageManager().getPackageInfo(
     * "com.shienh.rajbir.smokescreen",
     * PackageManager.GET_SIGNATURES);
     * for (Signature signature : info.signatures) {
     * MessageDigest md = MessageDigest.getInstance("SHA");
     * md.update(signature.toByteArray());
     * String sign = Base64
     * .encodeToString(md.digest(), Base64.DEFAULT);
     * Log.e("KEYHASH:", sign);
     * Toast.makeText(getApplicationContext(), sign, Toast.LENGTH_LONG)
     * .show();
     * }
     * } catch (PackageManager.NameNotFoundException e) {
     * e.printStackTrace();
     * } catch (NoSuchAlgorithmException e) {
     * e.printStackTrace();
     * }
     * }
     **/

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            mDialog.setMessage("Signing in...");
            mDialog.show();
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                mDialog.dismiss();
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                mDialog.dismiss();
                Snackbar.make(mBaseView, "Google sign in failed", 3000).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        mDialog.show();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI();
                        } else {
                            checkUserExists();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(mBaseView, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                        mDialog.dismiss();
                    }
                });
    }

    private void checkLogin() {
        String email = mLoginEmail.getText().toString().trim();
        String pass = mLoginPass.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Snackbar.make(mBaseView, "Email cannot be empty...", SNACK_DURATION).show();
        } else if (TextUtils.isEmpty(pass)) {
            Snackbar.make(mBaseView, "Password cannot be empty...", SNACK_DURATION).show();
        } else {
            mDialog.setMessage("Signing in...");
            mDialog.show();
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        checkUserExists();
                        mDialog.dismiss();
                    } else {
                        mDialog.dismiss();
                        Snackbar.make(mBaseView, "Error logging in...", SNACK_DURATION).show();
                    }
                }
            });
        }

    }

    private void checkUserExists() {
        if (mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();
            mDataBaseUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(user_id)) {
                        updateUI();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Snackbar.make(mBaseView, "Signing progress cancelled", 3000).show();
                }
            });
        }
    }
}
