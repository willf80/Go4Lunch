package com.apiman.go4lunch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apiman.go4lunch.services.FireStoreUtils;
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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginActivity";

    // Firebase
    private FirebaseAuth mFirebaseAuth;

    // Google
    private GoogleSignInClient mGoogleSignInClient;

    // Facebook
    CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Facebook
        mCallbackManager = CallbackManager.Factory.create();
        facebookCallbackRegister();

        mFirebaseAuth = FirebaseAuth.getInstance();

//        signOut();
    }

    private void facebookCallbackRegister() {
        LoginManager
                .getInstance()
                .registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(TAG, loginResult.getAccessToken().getToken());
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.w(TAG, "Cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "FacebookException", error);
                authenticationFailedAction();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Google ActivityResult
        if(requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = accountTask.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);
            }catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                authenticationFailedAction();
            }
        }else {
            // Facebook ActivityResult
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithFacebook(AccessToken accessToken) {
        AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mFirebaseAuth
                .signInWithCredential(authCredential)
                .addOnCompleteListener(this::onTaskCompleteHandle);
    }

    private void onTaskCompleteHandle(Task<AuthResult> task) {
        if(task.isSuccessful()){
            // Sign in success, update UI with the signed-in user's information
            Log.d(TAG, "signInWithCredential:success");

            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

            Task<Void> voidTask = FireStoreUtils.saveUser(firebaseUser);
            if(voidTask == null){
                authenticationFailedAction();
                return;
            }

            voidTask.addOnSuccessListener(aVoid -> loginSuccessfully())
                    .addOnFailureListener(e -> authenticationFailedAction());
        }else {
            authenticationFailedAction();
        }
    }

    private void authenticationFailedAction() {
        // If sign in fails, display a message to the user.
        Snackbar.make(findViewById(R.id.main_layout), LoginActivity.this.getString(R.string.auth_failed), Snackbar.LENGTH_SHORT).show();
//        updateUI(null);
    }

    private void firebaseAuthWithGoogle(@NonNull GoogleSignInAccount signInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + signInAccount.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this::onTaskCompleteHandle);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        updateUI((currentUser));
    }

    @OnClick(R.id.btnGoogle)
    public void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @OnClick(R.id.btnFacebook)
    public void facebookSignIn() {
        LoginManager
                .getInstance()
                .logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
    }

    private void signOut() {
        mFirebaseAuth.signOut();

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(task -> updateUI(null));
    }

    private void updateUI(@Nullable FirebaseUser user) {
        if(user == null) {
            Toast.makeText(this, "Disconnected", Toast.LENGTH_LONG).show();
            return;
        }

        loginSuccessfully();
    }

    private void loginSuccessfully() {
        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
        startConnectedActivity();
    }

    private void startConnectedActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        finish();
    }
}
