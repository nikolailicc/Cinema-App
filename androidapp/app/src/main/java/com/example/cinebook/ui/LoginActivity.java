package com.example.cinebook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinebook.R;
import com.example.cinebook.api.ApiService;
import com.example.cinebook.api.RetrofitClient;
import com.example.cinebook.model.User;
import com.example.cinebook.util.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsername, editPassword;
    private Button btnLogin;
    private TextView btnToggleRegister;
    private ProgressBar progressBar;

    private boolean registerMode = false;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new SessionManager(this);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnToggleRegister = findViewById(R.id.btnToggleRegister);
        progressBar = findViewById(R.id.progressBar);

        if (session.isLoggedIn()) {
            goToMain();
            return;
        }

        btnLogin.setOnClickListener(v -> {
            if (registerMode) doRegister();
            else doLogin();
        });

        btnToggleRegister.setOnClickListener(v -> {
            registerMode = !registerMode;
            btnLogin.setText(registerMode ? R.string.register : R.string.login);
            btnToggleRegister.setText(registerMode ? R.string.have_account : R.string.no_account);
        });
    }

    private void doLogin() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.empty_field_error, Toast.LENGTH_SHORT).show();
            return;
        }

        // Privremeno cuvamo kredencijale da bi ih AuthInterceptor mogao da koristi
        session.saveSession(username, password, "USER");

        setLoading(true);
        ApiService api = RetrofitClient.getApiService(this);
        api.me().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    session.saveSession(username, password, user.getRole());
                    session.saveUserId(user.getId());
                    goToMain();
                } else {
                    session.clear();
                    Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                setLoading(false);
                session.clear();
                Toast.makeText(LoginActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void doRegister() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.empty_field_error, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        ApiService api = RetrofitClient.getApiService(this);
        api.register(body).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Registracija uspešna, prijavite se", Toast.LENGTH_SHORT).show();
                    registerMode = false;
                    btnLogin.setText(R.string.login);
                    btnToggleRegister.setText(R.string.no_account);
                } else {
                    Toast.makeText(LoginActivity.this, "Registracija nije uspela", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
