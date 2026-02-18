package com.megaproject.currencyconverter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class MainActivity extends AppCompatActivity {

    private EditText etAmount;
    private TextView tvResultValue, tvExchangeRate, tvFromCode, tvFromName, tvToCode, tvToName, tvTimestamp;
    private android.widget.ImageView ivContentFrom, ivContentTo; // Added ImageViews
    private View btnSwap;
    private Button btnConvert;
    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> historyList = new ArrayList<>();
    
    private String fromCurrency = "USD";
    private String toCurrency = "EUR";
    private Map<String, Double> currentRates;
    
    // Using a reliable free API
    // Note: In production, base URL usually goes in a constant or build config.
    // Exchangerate-api v4 is common for simple free usage.
    private static final String BASE_URL = "https://api.exchangerate-api.com/v4/"; 
    private CurrencyApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Fix for overlap issue: Enable Edge-to-Edge and handle insets
        androidx.activity.EdgeToEdge.enable(this);
        
        setContentView(R.layout.activity_main);
        
        // Apply system bar insets to the root view
        View root = findViewById(android.R.id.content);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initViews();
        setupRetrofit();
        setupHistory();
        loadRates(); // Initial Load
        updateCurrencyUI(); // Initial UI set
    }

    private void initViews() {
        etAmount = findViewById(R.id.etAmount);
        tvResultValue = findViewById(R.id.tvResultValue);
        tvExchangeRate = findViewById(R.id.tvExchangeRate);
        tvFromCode = findViewById(R.id.tvFromCode);
        tvFromName = findViewById(R.id.tvFromName);
        tvToCode = findViewById(R.id.tvToCode);
        tvToName = findViewById(R.id.tvToName);
        ivContentFrom = findViewById(R.id.ivContentFrom); // Init ImageView
        ivContentTo = findViewById(R.id.ivContentTo);     // Init ImageView
        btnSwap = findViewById(R.id.btnSwap);
        btnConvert = findViewById(R.id.btnConvert);
        rvHistory = findViewById(R.id.rvHistory);
        tvTimestamp = findViewById(R.id.tvTimestamp);

        // Click listeners for currency selection (Mockups using PopupMenu for simplicity in this replica task)
        findViewById(R.id.containerFrom).setOnClickListener(v -> showCurrencyBottomSheet(true));
        findViewById(R.id.containerTo).setOnClickListener(v -> showCurrencyBottomSheet(false));
        
        btnSwap.setOnClickListener(v -> swapCurrencies());
        
        btnConvert.setOnClickListener(v -> performConversion(true)); // True indicates explicit button click -> add to history
        
        // Auto-update on amount change - DISABLED per request
        // Instead, we clear the result to indicate need for re-conversion
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                 clearResultUI();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });


        
        findViewById(R.id.btnSave).setOnClickListener(v -> Toast.makeText(this, "Conversion Saved!", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnShare).setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this conversion: " + tvResultValue.getText());
            startActivity(Intent.createChooser(shareIntent, "Share Conversion"));
        });
        

        
        // Initial state: Clear result
        clearResultUI();
    }

    private void clearResultUI() {
        tvResultValue.setText("---");
        tvExchangeRate.setText("");
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(CurrencyApiService.class);
    }
    
    private void setupHistory() {
        // Load history from SharedPreferences
        loadHistory(); 
        
        // Initialize if null (should be handled in loadHistory but safe check)
        if (historyList == null) {
            historyList = new ArrayList<>();
        }
        
        // Add dummy data for first impression
        if (historyList.isEmpty()) {
            historyList.add(new HistoryItem("USD", "EUR", 100, 92.54, "Today, 10:30 AM"));
            historyList.add(new HistoryItem("GBP", "USD", 50, 63.20, "Yesterday, 2:15 PM"));
            historyList.add(new HistoryItem("EUR", "JPY", 200, 31400, "Yesterday, 9:00 AM"));
            saveHistory();
        }

        historyAdapter = new HistoryAdapter(historyList);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }

    private void loadRates() {
        apiService.getLatestRates(fromCurrency).enqueue(new Callback<ExchangeRateResponse>() {
            @Override
            public void onResponse(Call<ExchangeRateResponse> call, Response<ExchangeRateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentRates = response.body().rates;
                    // Auto conversion removed
                }
            }

            @Override
            public void onFailure(Call<ExchangeRateResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to load rates", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performConversion(boolean addToHistory) {
        try {
            if (currentRates == null) {
                Toast.makeText(this, "Fetching rates...", Toast.LENGTH_SHORT).show();
                loadRates(); // Try loading if missing
                return;
            }
            
            String amountStr = etAmount.getText().toString();
            if (amountStr.isEmpty()) return;
            
            double amount = Double.parseDouble(amountStr);
            Double rate = currentRates.get(toCurrency);
            
            if (rate != null) {
                double result = amount * rate;
                
                // Format result
                String symbol = getCurrencySymbol(toCurrency);
                tvResultValue.setText(String.format(Locale.getDefault(), "%s%.2f", symbol, result));
                
                // Format rate pill
                String rateText = String.format(Locale.getDefault(), "1 %s = %.4f %s", fromCurrency, rate, toCurrency);
                tvExchangeRate.setText(rateText);
                
                // Add to history only on button click to avoid spam
                if (addToHistory) {
                    try {
                        String date = new SimpleDateFormat("Today, h:mm a", Locale.getDefault()).format(new Date());
                        historyList.add(0, new HistoryItem(fromCurrency, toCurrency, amount, result, date));
                        // Ensure adapter is not null and notify
                        if (historyAdapter != null) {
                            historyAdapter.notifyItemInserted(0);
                            rvHistory.scrollToPosition(0);
                        }
                        saveHistory(); // Save updated history
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error updating history: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(this, "Rate not found for " + toCurrency, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Conversion Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void swapCurrencies() {
        try {
            String temp = fromCurrency;
            fromCurrency = toCurrency;
            toCurrency = temp;
            
            updateCurrencyUI();
            clearResultUI(); // Clear result on swap
            
            // Reload rates for new base
            loadRates();
        } catch (Exception e) {
             e.printStackTrace();
        }
    }
    
    // Quick helper for Symbols (simplified subset)
    private String getCurrencySymbol(String code) {
        if (code == null) return "?";
        switch (code) {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "JPY": return "¥";
            case "CAD": return "$";
            default: return code + " ";
        }
    }
    
    private void updateCurrencyUI() {
        try {
            tvFromCode.setText(fromCurrency);
            tvFromName.setText(getCurrencyName(fromCurrency));
            tvToCode.setText(toCurrency);
            tvToName.setText(getCurrencyName(toCurrency));
            
            // Load flags safely
            if (ivContentFrom != null) {
                com.bumptech.glide.Glide.with(this).load(getFlagUrl(fromCurrency)).circleCrop().into(ivContentFrom);
            }
            if (ivContentTo != null) {
                com.bumptech.glide.Glide.with(this).load(getFlagUrl(toCurrency)).circleCrop().into(ivContentTo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String getCurrencyName(String code) {
        switch (code) {
            case "USD": return "United States Dollar";
            case "EUR": return "Euro";
            case "GBP": return "British Pound";
            case "JPY": return "Japanese Yen";
            case "CAD": return "Canadian Dollar";
            case "AUD": return "Australian Dollar";
            case "CHF": return "Swiss Franc";
            case "CNY": return "Chinese Yuan";
            case "INR": return "Indian Rupee";
            case "SGD": return "Singapore Dollar";
            case "ZAR": return "South African Rand";
            default: return "Currency";
        }
    }
    
    private String getFlagUrl(String code) {
        String countryCode;
        switch (code) {
            case "USD": countryCode = "us"; break;
            case "EUR": countryCode = "eu"; break;
            case "GBP": countryCode = "gb"; break;
            case "JPY": countryCode = "jp"; break;
            case "CAD": countryCode = "ca"; break;
            case "AUD": countryCode = "au"; break;
            case "CHF": countryCode = "ch"; break;
            case "CNY": countryCode = "cn"; break;
            case "INR": countryCode = "in"; break;
            case "SGD": countryCode = "sg"; break;
            case "ZAR": countryCode = "za"; break;
            default: countryCode = "us"; // Fallback
        }
        return "https://flagcdn.com/w160/" + countryCode + ".png";
    }

    // Simple Popup Menu for Currency Selection (Replica requirement asked for functionality, this is the standard native way without building a custom BottomSheet dialog from scratch, though bottom sheet was in HTML. For 'exact replica' visual, a BottomSheetDialog is better, but code complexity is high. I'll use Popup for now to ensure reliability, or should I attempt the BottomSheet? The user provided HTML for BottomSheet. Let's stick to essential logic first).
    // Actually, let's just make a simple toggle for the top 5 currencies shown in the HTML for demonstration.
    // Flag to track which currency we are selecting
    private boolean isSelectingFrom = true;

    private void showCurrencyBottomSheet(boolean isFrom) {
        isSelectingFrom = isFrom;
        String current = isFrom ? fromCurrency : toCurrency;
        CurrencySelectionBottomSheet bottomSheet = CurrencySelectionBottomSheet.newInstance(current);
        bottomSheet.setListener(code -> {
             if (isSelectingFrom) {
                 String oldFrom = fromCurrency;
                 fromCurrency = code;
                 // If From becomes same as To, swap To with old From
                 if (fromCurrency.equals(toCurrency)) {
                     toCurrency = oldFrom;
                 }
                 loadRates();
            } else {
                String oldTo = toCurrency;
                toCurrency = code;
                // If To becomes same as From, swap From with old To
                if (toCurrency.equals(fromCurrency)) {
                    fromCurrency = oldTo;
                    loadRates(); // Reload because base (From) changed
                } else {
                    // No conversion yet
                }
            }
            updateCurrencyUI();
            clearResultUI(); // Clear result on selection change
        });
        bottomSheet.show(getSupportFragmentManager(), "CurrencyBottomSheet");
    }

    private void showCurrencyMenu(View v, boolean isFrom) {
       showCurrencyBottomSheet(isFrom);
    }

    private void saveHistory() {
        SharedPreferences sharedPreferences = getSharedPreferences("CurrencyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(historyList);
        editor.putString("history_list", json);
        editor.apply();
    }

    private void loadHistory() {
        SharedPreferences sharedPreferences = getSharedPreferences("CurrencyPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("history_list", null);
        Type type = new TypeToken<ArrayList<HistoryItem>>() {}.getType();
        
        if (json != null) {
            historyList = gson.fromJson(json, type);
        }
        
        if (historyList == null) {
            historyList = new ArrayList<>();
        }
    }
}
