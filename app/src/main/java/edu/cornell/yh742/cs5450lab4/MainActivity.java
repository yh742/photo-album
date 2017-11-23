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
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mWelcomeText = (TextView) findViewById(R.id.welcome);
        mLoginButton = (Button) findViewById(R.id.signin_button);
        mLoginGuest = (Button) findViewById(R.id.guest_button);
        mSignOut = (Button) findViewById(R.id.signout_button);
        mSignOut.setEnabled(false);
        if (mAuth.getCurrentUser() != null) {
            mSignOut.setEnabled(true);
        }

        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                mSignOut.setEnabled(false);
            }
        });

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

        mLoginGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                mSignOut.setEnabled(true);
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
