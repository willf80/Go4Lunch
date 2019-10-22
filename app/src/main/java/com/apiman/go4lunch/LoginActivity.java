package com.apiman.go4lunch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.apiman.go4lunch.helpers.FireStoreUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Twitter issue https://stackoverflow.com/questions/51199158/callback-url-not-approved-for-this-client-application-in-android-firebase-twitt
 */
public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_CREATE_ACCOUNT = 70000;
    private static final String TAG = "LoginActivity";

    private static final List<String> facebookPermission = Arrays.asList("public_profile", "email");

    @BindView(R.id.userNameEditText)
    TextInputEditText userNameEditText;

    @BindView(R.id.passwordEditText)
    TextInputEditText passwordEditText;

    @BindView(R.id.twitterLoginButton)
    TwitterLoginButton twitterLoginButton;

    // Firebase
    private FirebaseAuth mFirebaseAuth;

    // Google
    private GoogleSignInClient mGoogleSignInClient;

    // Facebook
    CallbackManager mCallbackManager;

    Exception firebaseUserIsNullException = new Exception("Firebase user is null");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // Google
        mGoogleSignInClient = FireStoreUtils.getGoogleSignInClient(this);

        // Facebook
        mCallbackManager = CallbackManager.Factory.create();
        facebookCallbackRegister();

        // Twitter
        Twitter.initialize(FireStoreUtils.getTwitterConfig(this));
        twitterLoginButton.setCallback(mTwitterSessionCallback);

        mFirebaseAuth = FirebaseAuth.getInstance();
        if(mFirebaseAuth.getCurrentUser() != null) {
            startConnectedActivity();
        }
    }

    private Callback<TwitterSession> mTwitterSessionCallback = new Callback<TwitterSession>() {
        @Override
        public void success(Result<TwitterSession> result) {
            firebaseAuthWithTwitter(result.data);
        }

        @Override
        public void failure(TwitterException exception) {
            authenticationFailedAction(exception);
        }
    };

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
                    authenticationFailedAction(error);
                }
            });
    }

    private void connectedAfterCreateNewAccount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        startConnectedActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_CREATE_ACCOUNT && resultCode == CreateAccountActivity.RESULT_CODE_CREATE_ACCOUNT) {
            connectedAfterCreateNewAccount();
            return;
        }

        // Google ActivityResult
        if(requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = accountTask.getResult(ApiException.class);
                assert account != null;
                firebaseAuthWithGoogle(account);
            }catch (Exception e) {
                authenticationFailedAction(e);
            }
        } else {
            // Facebook ActivityResult
            mCallbackManager.onActivityResult(requestCode, resultCode, data);

            // Twitter
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithFacebook(AccessToken accessToken) {
        AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mFirebaseAuth
                .signInWithCredential(authCredential)
                .addOnCompleteListener(this::onTaskCompleteHandle);
    }

    private void firebaseAuthWithGoogle(@NonNull GoogleSignInAccount signInAccount) {
        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this::onTaskCompleteHandle);
    }

    private void firebaseAuthWithUsernamePassword(String email, String password) {
        if((email == null || email.isEmpty())) {
            Toast.makeText(this, this.getString(R.string.email_required_to_login), Toast.LENGTH_LONG).show();
            return;
        }

        if((password == null || password.isEmpty())) {
            Toast.makeText(this, this.getString(R.string.password_required_to_login), Toast.LENGTH_LONG).show();
            return;
        }

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this::onTaskCompleteHandle);
    }

    private void firebaseAuthWithTwitter(TwitterSession twitterSession) {
        AuthCredential credential = TwitterAuthProvider.getCredential(
                twitterSession.getAuthToken().token,
                twitterSession.getAuthToken().secret);

        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this::onTaskCompleteHandle);
    }

    private void onTaskCompleteHandle(Task<AuthResult> task) {
        if(task.isSuccessful()){
            // Sign in success, update UI with the signed-in user's information
            Log.d(TAG, "signInWithCredential:success");
            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

            Task<Void> voidTask = FireStoreUtils.saveUser(firebaseUser);
            if(voidTask == null){
                authenticationFailedAction(firebaseUserIsNullException);
                return;
            }

            voidTask.addOnSuccessListener(aVoid -> loginSuccessfully())
                    .addOnFailureListener(this::authenticationFailedAction);
        }else {
            authenticationFailedAction(task.getException());
        }
    }

    private void authenticationFailedAction(Exception e) {
        // If sign in fails, display a message to the user.
        String message = LoginActivity.this.getString(R.string.auth_failed_default_message);
        if(e instanceof FirebaseAuthInvalidCredentialsException) {
            message = LoginActivity.this.getString(R.string.invalid_username_or_password);
        }

        Snackbar.make(findViewById(R.id.main_layout), message, Snackbar.LENGTH_SHORT).show();
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
                .logInWithReadPermissions(this, facebookPermission);
    }

    @OnClick(R.id.btnTwitter)
    public void twitterSignIn() {
        twitterLoginButton.performClick();
    }

    @OnClick(R.id.btnLogin)
    public void usernamePasswordSignIn() {
        String email = Objects.requireNonNull(userNameEditText.getText()).toString();
        String password = Objects.requireNonNull(passwordEditText.getText()).toString();

        firebaseAuthWithUsernamePassword(email, password);
    }

    @OnClick(R.id.btnCreateAccount)
    public void createNewAccount() {
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivityForResult(intent, RC_CREATE_ACCOUNT);
    }

    private void loginSuccessfully() {
        Toast.makeText(this, this.getString(R.string.connected), Toast.LENGTH_LONG).show();
        startConnectedActivity();
    }

    private void startConnectedActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        finish();
    }
}
