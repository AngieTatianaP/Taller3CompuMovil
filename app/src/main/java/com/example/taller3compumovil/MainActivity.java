package com.example.taller3compumovil;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.taller3compumovil.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    private static final String TAG = MainActivity.class.getName();
    private FirebaseAuth mAuth;
    
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginBTN.setVisibility(View.GONE);
        binding.registerBTN.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        } else {
            binding.loginBTN.setVisibility(View.VISIBLE);
            binding.registerBTN.setVisibility(View.VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateUI(FirebaseUser currentUser) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) + ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            if (currentUser != null) {
                Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                intent.putExtra("user", currentUser.getEmail());
                startActivity(intent);
                System.out.println("Bienvenido" + currentUser.getEmail());

            } else {
                binding.loginEmail.setText("");
                binding.loginPass.setText("");
            }
        }
    }

    private boolean validateForm() {
        boolean valid = true;
        String email = binding.loginEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            binding.loginEmail.setError("Required.");
            valid = false;
        } else {
            binding.loginEmail.setError(null);
        }
        String password = binding.loginPass.getText().toString();
        if (TextUtils.isEmpty(password)) {
            binding.loginPass.setError("Required.");
            valid = false;
        } else {
            binding.loginPass.setError(null);
        }
        return valid;
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void signInUser(String email, String password) {
        if (validateForm()) {
            progressDialog.setMessage("Iniciando sesión...");
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            progressDialog.dismiss();
                            updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            updateUI(null);
                        }
                    });
        }
    }

    public static boolean isEmailValid(String emailStr) {
        return (!TextUtils.isEmpty(emailStr) && Patterns.EMAIL_ADDRESS.matcher(emailStr).matches());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void login(View view) {
        String email = binding.loginEmail.getText().toString();
        String pass = binding.loginPass.getText().toString();

        if (!isEmailValid(email)) {
            Toast.makeText(MainActivity.this, "Email is not a valid format",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        signInUser(email, pass);
    }

    public void registrarse(View view) {
        Intent intent = new Intent(this, SigninActivity.class);
        startActivity(intent);
    }

    private void requestPermission(Activity context,
                                   String[] permissions, int idCode) {
        boolean flag = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                flag = true;
                break;
            }
        }
        if (flag) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Grant those permissions");
            builder.setMessage("Location neeeded");
            builder.setPositiveButton("ok", (dialogInterface, i) -> ActivityCompat.requestPermissions(context, permissions, idCode));
            builder.setNegativeButton("cancel", (dialogInterface, i) -> dialogInterface.dismiss());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            ActivityCompat.requestPermissions(context, permissions, idCode);

        }
    }

}