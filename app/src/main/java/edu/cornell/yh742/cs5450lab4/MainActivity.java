package edu.cornell.yh742.cs5450lab4;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.StringDef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PATH_TOS = "";
    private Button mLoginButton;
    private Button mLoginGuest;
    private Button mSignOut;
    private TextView mWelcomeText;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // get security authentication instance
        mAuth = FirebaseAuth.getInstance();

        // get UI components
        mWelcomeText = (TextView) findViewById(R.id.welcome);
        mLoginButton = (Button) findViewById(R.id.signin_button);
        mLoginGuest = (Button) findViewById(R.id.guest_button);
        mSignOut = (Button) findViewById(R.id.signout_button);
        mSignOut.setEnabled(false);

        // if the user is signed in already
        if (mAuth.getCurrentUser() != null) {
            mWelcomeText.setText("Welcome! " + mAuth.getCurrentUser().getEmail());
            mLoginButton.setText("Login With Another Account");
            mSignOut.setEnabled(true);
        }

        // sign out the user
        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                mWelcomeText.setText("Welcome!");
                mLoginButton.setText(R.string.signin_text);
                mSignOut.setEnabled(false);
            }
        });

        // launches Firebase UI authentication
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() == null) {
                    startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                            .build(), RC_SIGN_IN);
                }
                else{
                    Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(intent);
                }
            }
        });

        // launches Guest login authentication
        mLoginGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });
    }

    // when guest login is finished, this even will fire
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                // display user name
                mWelcomeText.setText("Welcome! " + mAuth.getCurrentUser().getEmail());
                mLoginButton.setText("Sign In With Another Account");
                mSignOut.setEnabled(true);
                // go back to main menu
                finish();
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(intent);
            }
            else{
                Log.d(TAG, "Didn't enter password");
            }
            return;
        }
    }
}
