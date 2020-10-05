package de.threateningcodecomments.routinetimer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseUser account;
    private GoogleSignInOptions gso;

    private MaterialButton setupButton;
    private MaterialButton testButton;
    private ShapeableImageView profilepicview;
    private MaterialTextView usernameView;
    private MaterialCardView nameCardView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGSignIn();

        initBufferViews();

        initOnClicks();

        if (account == null) {
            signIn();
        }
    }

    //region init
    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null

        updateUI();
    }

    private void initBufferViews() {
        setupButton = findViewById(R.id.btn_mainActivity_setup);
        testButton = findViewById(R.id.btn_mainActivity_test);
        usernameView = findViewById(R.id.tv_MainActivity_username);
        profilepicview = findViewById(R.id.iv_MainActivity_profilepic);
        profilepicview.getShapeAppearanceModel();
        nameCardView = findViewById(R.id.cv_MainActivity_name);
    }

    private void initOnClicks() {
        setupButton.setOnClickListener(this);
        nameCardView.setOnClickListener(this);
        testButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_mainActivity_setup:
                Intent intent = new Intent(MainActivity.this, SetRoutine.class);
                startActivity(intent);
                break;

            case R.id.cv_MainActivity_name:
                toggleSignIn();
                break;

            case R.id.btn_mainActivity_test:
                ResourceClass.loadRoutines();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {


                        ArrayList<Routine> routines = ResourceClass.getRoutines();
                        StringBuilder sb = new StringBuilder();
                        sb.append("Loaded Routines: ");

                        for (Routine tmpRoutine : routines) {
                            sb.append(tmpRoutine.getName()).append("; ");
                        }

                        Toast.makeText(MainActivity.this, sb, Toast.LENGTH_LONG).show();

                    }
                }, 5000);
                break;

            default:
                Toast.makeText(this, "Unknown Error, please see developer or priest", Toast.LENGTH_LONG).show();
        }
    }

    private void initGSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
    }
    //endregion

    //region Greeting
    private void updateUI() {
        account = FirebaseAuth.getInstance().getCurrentUser();

        if (account == null) {
            handleGreetNoAccount();
        } else {
            handleGreetAccount();
        }

    }

    private void handleGreetAccount() {
        String message;
        message = "Welcome " + account.getDisplayName() + "!";

        Uri pathToPhoto = account.getPhotoUrl();
        Glide.with(this).load(pathToPhoto).into(profilepicview);

        Glide.with(this).asBitmap().load(pathToPhoto).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                int pixelColor = bitmap.getPixel(0, 0);

                nameCardView.setCardBackgroundColor(pixelColor);
            }

            @Override
            public void onLoadCleared(Drawable placeholder) {
            }
        });

        usernameView.setText(message);
    }

    private void handleGreetNoAccount() {
        Drawable errorDrawable = getResources().getDrawable(R.drawable.ic_defaultdrawable);
        profilepicview.setImageDrawable(errorDrawable);

        usernameView.setText("Please log in!");

        int bgColor;

        if (ResourceClass.isNightMode(getApplication())) {
            bgColor = Tile.DEFAULT_COLOR_DARK;
        } else {
            bgColor = Tile.DEFAULT_COLOR;
        }

        nameCardView.setCardBackgroundColor(bgColor);
    }
    //endregion

    //region handle sign in
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                MyLog.f("firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                MyLog.fw("Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            MyLog.f("signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            MyLog.fw("signInWithCredential:failure", task.getException());
                            updateUI();
                        }

                        // ...
                    }
                });
    }

    private void toggleSignIn() {
        updateUI();

        if (account == null) {
            signIn();
        } else {
            signOut();
        }

        updateUI();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        MyLog.d("Logged out of google account!");
                    }
                });

        Toast.makeText(this, "Signed out!", Toast.LENGTH_LONG).show();
    }
    //endregion
}