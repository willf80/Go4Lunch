package com.apiman.go4lunch;

import android.os.Bundle;
import android.text.Editable;
import android.widget.Toast;

import com.apiman.go4lunch.fragments.ProgressDialogFragment;
import com.apiman.go4lunch.helpers.FireStoreUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateAccountActivity extends BaseActivity {

    public final static int RESULT_CODE_CREATE_ACCOUNT = 70001;

    @BindView(R.id.fullNameEditText)
    TextInputEditText fullNameEditText;

    @BindView(R.id.userNameEditText)
    TextInputEditText userNameEditText;

    @BindView(R.id.passwordEditText)
    TextInputEditText passwordEditText;

    @BindView(R.id.passwordBisEditText)
    TextInputEditText passwordBisEditText;

    String fullName;
    ProgressDialogFragment mProgressDialogFragment;
    Exception firebaseUserIsNullException = new Exception("Firebase user is null");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        ButterKnife.bind(this);

        displayHomeAsUp();

        mProgressDialogFragment = ProgressDialogFragment.newInstance();
    }

    private String extraInputData(TextInputEditText inputEditText) {
        Editable text = inputEditText.getText();
        if (text == null) {
            return null;
        }

        return text.toString();
    }

    private String checkUserInfo(String fullName, String email, String password, String passwordBis) {

        if (fullName == null || fullName.isEmpty()) {
            return this.getString(R.string.full_name_required);
        }

        if (email == null || email.isEmpty()) {
            return this.getString(R.string.email_is_required);
        }

        if (password == null || password.isEmpty()) {
            return this.getString(R.string.password_is_required);
        }

        if (password.length() < 6) {
            return this.getString(R.string.minimum_characters_required);
        }

        if (!Objects.equals(password, passwordBis)) {
            return this.getString(R.string.not_same_password);
        }

        return null; // No error
    }

    @OnClick(R.id.btnValidate)
    public void onValidate() {

        // Start the progress dialog
        mProgressDialogFragment.show(getSupportFragmentManager());

        // Extraction
        fullName = extraInputData(fullNameEditText);
        String email = extraInputData(userNameEditText);
        String password = extraInputData(passwordEditText);
        String passwordBis = extraInputData(passwordBisEditText);


        String response = checkUserInfo(fullName, email, password, passwordBis);
        if(response != null) {
            Toast.makeText(this, response, Toast.LENGTH_LONG).show();
            mProgressDialogFragment.dismiss();
            return;
        }

        // Do inscription
        createAccountOnFirebase(email, password);
    }

    private void createAccountOnFirebase(String email, String password) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this::onTaskCompleteHandle);
    }

    private void onTaskCompleteHandle(Task<AuthResult> task) {
        if(task.isSuccessful()){
            // Sign in success, update UI with the signed-in user's information
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            Task<Void> voidTask = FireStoreUtils.saveUser(firebaseUser);
            if(voidTask == null){
                accountCreationFailed(firebaseUserIsNullException);
                return;
            }

            voidTask.addOnSuccessListener(aVoid -> updateUserInfo())
                    .addOnFailureListener(this::accountCreationFailed);
        }else {
            accountCreationFailed(task.getException());
        }
    }

    private void updateUserInfo() {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

        FirebaseUser firebaseUser = FirebaseAuth
                .getInstance()
                .getCurrentUser();

        if(firebaseUser == null) {
            accountCreationFailed(firebaseUserIsNullException);
            return;
        }

        firebaseUser
                .updateProfile(profileUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Show successfully message
                    Toast.makeText(this, this.getString(R.string.account_created_succcessfully), Toast.LENGTH_LONG).show();

                    mProgressDialogFragment.dismiss();
                    setResult(RESULT_CODE_CREATE_ACCOUNT);
                    finish();
                })
                .addOnFailureListener(this::accountCreationFailed);
    }

    private void accountCreationFailed(Exception e) {
        mProgressDialogFragment.dismiss();
        if(e instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(this, this.getString(R.string.email_already_used), Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, this.getString(R.string.new_account_creation_failed), Toast.LENGTH_LONG).show();
    }
}
