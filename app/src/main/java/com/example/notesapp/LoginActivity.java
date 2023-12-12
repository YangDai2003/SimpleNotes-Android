package com.example.notesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.DynamicColors;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author 30415
 */
public class LoginActivity extends AppCompatActivity {
    private final Executor executor = Executors.newSingleThreadExecutor();
    SpinKitView spinKitView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        setContentView(R.layout.activity_login);
        spinKitView = findViewById(R.id.spin_kit);

        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("usePassword", false)) {
            spinKitView.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();
            }, 600);
        } else {
            biometricLogin();
            MaterialButton materialButton = findViewById(R.id.materialButton);
            materialButton.setOnClickListener(v -> biometricLogin());
        }
    }

    private void biometricLogin() {
        BiometricPrompt biometricPrompt;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            biometricPrompt = new BiometricPrompt.Builder(this)
                    .setTitle(getString(R.string.login))
                    .setConfirmationRequired(false)
                    .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL | BiometricManager.Authenticators.BIOMETRIC_WEAK)
                    .build();
        } else {
            biometricPrompt = new BiometricPrompt.Builder(this)
                    .setTitle(getString(R.string.login))
                    .setConfirmationRequired(false)
                    .setDeviceCredentialAllowed(true)
                    .build();
        }
        biometricPrompt.authenticate(new CancellationSignal(), executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}