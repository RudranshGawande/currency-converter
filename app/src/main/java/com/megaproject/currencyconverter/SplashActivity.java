package com.megaproject.currencyconverter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide ActionBar if present
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Metallic Text Gradient for Title
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) {
            float textSize = tvTitle.getTextSize();
            Shader textShader = new LinearGradient(0, 0, 0, textSize,
                    new int[]{
                            Color.parseColor("#FFFFFF"),
                            Color.parseColor("#A5B4FC"),
                            Color.parseColor("#FFFFFF")
                    },
                    new float[]{0.2f, 0.5f, 0.8f},
                    Shader.TileMode.CLAMP);
            tvTitle.getPaint().setShader(textShader);
        }

        // Navigate to MainActivity after 3 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}
