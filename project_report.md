# Currency Converter Project Report

This document contains the source code for the Currency Converter Android application (Java, Layouts, Values, and Manifest).

## Java Code

### java\com\megaproject\currencyconverter\CurrencyAdapter.java

```java
package com.megaproject.currencyconverter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CurrencyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CurrencyItem> items = new ArrayList<>();
    private String selectedCurrencyCode;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CurrencyItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<CurrencyItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
    
    public void setSelectedCurrency(String code) {
        this.selectedCurrencyCode = code;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == CurrencyItem.TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currency_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currency, parent, false);
            return new ItemViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CurrencyItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvTitle.setText(item.getHeaderTitle());
        } else if (holder instanceof ItemViewHolder) {
            ItemViewHolder vh = (ItemViewHolder) holder;
            vh.tvCode.setText(item.getCode());
            vh.tvName.setText(item.getName());
            
            Glide.with(vh.itemView.getContext())
                .load(item.getFlagUrl())
                .circleCrop()
                .into(vh.ivFlag);

            boolean isSelected = item.getCode().equalsIgnoreCase(selectedCurrencyCode);
            vh.ivCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            
            vh.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        HeaderViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHeaderTitle);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFlag, ivCheck;
        TextView tvCode, tvName;
        ItemViewHolder(View itemView) {
            super(itemView);
            ivFlag = itemView.findViewById(R.id.ivFlag);
            ivCheck = itemView.findViewById(R.id.ivCheck);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
}
```

### java\com\megaproject\currencyconverter\CurrencyApiService.java

```java
package com.megaproject.currencyconverter;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CurrencyApiService {
    @GET("latest/{base}")
    Call<ExchangeRateResponse> getLatestRates(@Path("base") String base);
}
```

### java\com\megaproject\currencyconverter\CurrencyItem.java

```java
package com.megaproject.currencyconverter;

public class CurrencyItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private int type;
    private String headerTitle;

    private String code;
    private String name;
    private String flagUrl;

    // Constructor for Header
    public CurrencyItem(String headerTitle) {
        this.type = TYPE_HEADER;
        this.headerTitle = headerTitle;
    }

    // Constructor for Item
    public CurrencyItem(String code, String name, String flagUrl) {
        this.type = TYPE_ITEM;
        this.code = code;
        this.name = name;
        this.flagUrl = flagUrl;
    }

    public int getType() { return type; }
    public String getHeaderTitle() { return headerTitle; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getFlagUrl() { return flagUrl; }
}
```

### java\com\megaproject\currencyconverter\CurrencySelectionBottomSheet.java

```java
package com.megaproject.currencyconverter;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class CurrencySelectionBottomSheet extends BottomSheetDialogFragment {

    private CurrencyAdapter adapter;
    private List<CurrencyItem> allItems = new ArrayList<>();
    private String currentSelection;
    private OnCurrencySelectedListener listener;

    public interface OnCurrencySelectedListener {
        void onCurrencySelected(String currencyCode);
    }

    public static CurrencySelectionBottomSheet newInstance(String currentSelection) {
        CurrencySelectionBottomSheet fragment = new CurrencySelectionBottomSheet();
        Bundle args = new Bundle();
        args.putString("selection", currentSelection);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCurrencySelectedListener) {
            listener = (OnCurrencySelectedListener) context;
        }
    }

    // Allow setting listener manually if not attached to context
    public void setListener(OnCurrencySelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentSelection = getArguments().getString("selection", "USD");
        }
        setupData();
    }
    
    @Override 
    public int getTheme() {
        return R.style.CustomBottomSheetDialogTheme;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_currency_selection_bottom_sheet, container, false);
        
        RecyclerView rv = view.findViewById(R.id.rvCurrencies);
        EditText etSearch = view.findViewById(R.id.etSearch);
        View btnDone = view.findViewById(R.id.btnDone);
        
        adapter = new CurrencyAdapter();
        adapter.updateData(allItems);
        adapter.setSelectedCurrency(currentSelection); // Set initial selection
        
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);
        
        adapter.setOnItemClickListener(item -> {
            if (listener != null) {
                listener.onCurrencySelected(item.getCode());
                // We don't dismiss immediately, per standard UX, user sees checkmark update.
                // But the HTML "Done" button suggests maybe confirmation is needed? 
                // Or maybe selection updates immediately and Done just closes.
                // The screenshot shows a checkmark.
                // Let's update the checkmark locally.
                currentSelection = item.getCode();
                adapter.setSelectedCurrency(currentSelection);
                
                // If we want immediate return, uncomment:
                // dismiss();
            }
        });
        
        btnDone.setOnClickListener(v -> {
            // If we didn't notify on click, we notify here. But it's better to notify on click (reactive) and just close here.
            // If the listener was called on click, we just dismiss here.
            dismiss();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }
    
    private void setupData() {
        allItems.clear();
        allItems.add(new CurrencyItem("POPULAR"));
        allItems.add(new CurrencyItem("USD", "United States Dollar", "https://flagcdn.com/w160/us.png"));
        allItems.add(new CurrencyItem("EUR", "Euro", "https://flagcdn.com/w160/eu.png"));
        allItems.add(new CurrencyItem("GBP", "British Pound Sterling", "https://flagcdn.com/w160/gb.png"));
        allItems.add(new CurrencyItem("JPY", "Japanese Yen", "https://flagcdn.com/w160/jp.png"));
        
        allItems.add(new CurrencyItem("ALL CURRENCIES"));
        allItems.add(new CurrencyItem("AUD", "Australian Dollar", "https://flagcdn.com/w160/au.png"));
        allItems.add(new CurrencyItem("CAD", "Canadian Dollar", "https://flagcdn.com/w160/ca.png"));
        allItems.add(new CurrencyItem("CHF", "Swiss Franc", "https://flagcdn.com/w160/ch.png"));
        allItems.add(new CurrencyItem("CNY", "Chinese Yuan", "https://flagcdn.com/w160/cn.png"));
        allItems.add(new CurrencyItem("INR", "Indian Rupee", "https://flagcdn.com/w160/in.png"));
        allItems.add(new CurrencyItem("SGD", "Singapore Dollar", "https://flagcdn.com/w160/sg.png"));
        allItems.add(new CurrencyItem("ZAR", "South African Rand", "https://flagcdn.com/w160/za.png"));
    }
    
    private void filter(String query) {
        if (query.isEmpty()) {
            adapter.updateData(allItems);
            return;
        }
        
        List<CurrencyItem> filtered = new ArrayList<>();
        String lower = query.toLowerCase();
        
        for (CurrencyItem item : allItems) {
            if (item.getType() == CurrencyItem.TYPE_HEADER) continue; // Skip headers in search
            
            if (item.getType() == CurrencyItem.TYPE_ITEM) {
                if (item.getCode().toLowerCase().contains(lower) || item.getName().toLowerCase().contains(lower)) {
                    filtered.add(item);
                }
            }
        }
        adapter.updateData(filtered);
    }
}
```

### java\com\megaproject\currencyconverter\ExchangeRateResponse.java

```java
package com.megaproject.currencyconverter;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ExchangeRateResponse {
    @SerializedName("base")
    public String base;
    
    @SerializedName("date")
    public String date;
    
    @SerializedName("rates")
    public Map<String, Double> rates;
}
```

### java\com\megaproject\currencyconverter\HistoryAdapter.java

```java
package com.megaproject.currencyconverter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistoryItem> historyList;

    public HistoryAdapter(List<HistoryItem> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        
        String fromText = String.format("%.2f %s", item.fromAmount, item.fromCode);
        String toText = String.format("%.2f %s", item.toAmount, item.toCode);
        
        holder.tvConversionPair.setText(fromText + " → " + toText);
        holder.tvDate.setText(item.date);
        
        // Simple manual coloring logic for icons based on position to match UI screenshot approximately
        int colorRes;
        switch (position % 3) {
            case 0: colorRes = R.color.success_text; break; // Greenish
            case 1: colorRes = R.color.primary; break;      // Blueish
            default: colorRes = R.color.deep_violet; break; // Purplish
        }
        holder.ivIcon.setColorFilter(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), colorRes));
        
        holder.btnDeleteHistory.setOnClickListener(v -> {
            historyList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, historyList.size());
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvConversionPair, tvDate;
        ImageView ivIcon, btnDeleteHistory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConversionPair = itemView.findViewById(R.id.tvConversionPair);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            btnDeleteHistory = itemView.findViewById(R.id.btnDeleteHistory);
        }
    }
}
```

### java\com\megaproject\currencyconverter\HistoryItem.java

```java
package com.megaproject.currencyconverter;

public class HistoryItem {
    public String fromCode;
    public String toCode;
    public double fromAmount;
    public double toAmount;
    public String date;

    public HistoryItem(String fromCode, String toCode, double fromAmount, double toAmount, String date) {
        this.fromCode = fromCode;
        this.toCode = toCode;
        this.fromAmount = fromAmount;
        this.toAmount = toAmount;
        this.date = date;
    }
}
```

### java\com\megaproject\currencyconverter\MainActivity.java

```java
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
```

### java\com\megaproject\currencyconverter\SplashActivity.java

```java
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
```

## Manifest

### AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.CurrencyConverter">
        <activity
            android:name=".SplashActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity
            android:name=".MainActivity"
            android:exported="false"
            android:windowSoftInputMode="adjustResize" />

    </application>
</manifest> 
```

## Layout Files

### res\layout\activity_main.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background_light"
    tools:context=".MainActivity">

    <!-- Top App Bar -->
    <androidx.constraintlayout.widget.ConstraintLayout
        android:id="@+id/appBar"
        android:layout_width="match_parent"
        android:layout_height="60dp"
        android:background="#CCF6F7F8"
        android:paddingHorizontal="16dp"
        app:layout_constraintTop_toTopOf="parent">



        <TextView
            android:id="@+id/tvAppTitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:fontFamily="sans-serif-medium"
            android:text="Currency Converter"
            android:textColor="@color/slate_900"
            android:textSize="18sp"
            android:textStyle="bold"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

    </androidx.constraintlayout.widget.ConstraintLayout>

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:scrollbars="none"
        android:clipToPadding="false"
        android:paddingBottom="100dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintTop_toBottomOf="@id/appBar">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <!-- Converter Card -->
            <androidx.constraintlayout.widget.ConstraintLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@drawable/bg_card_rounded"
                android:elevation="2dp"
                android:padding="20dp">

                <!-- Amount Label -->
                <TextView
                    android:id="@+id/tvLabelAmount"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="AMOUNT"
                    android:textColor="@color/slate_500"
                    android:textSize="12sp"
                    android:textStyle="bold"
                    app:layout_constraintStart_toStartOf="parent"
                    app:layout_constraintTop_toTopOf="parent" />

                <!-- Amount Input -->
                <LinearLayout
                    android:id="@+id/amountContainer"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:background="@drawable/bg_input_rounded"
                    android:gravity="center_vertical"
                    android:orientation="horizontal"
                    android:paddingHorizontal="16dp"
                    android:paddingVertical="12dp"
                    app:layout_constraintTop_toBottomOf="@id/tvLabelAmount">

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="$"
                        android:textColor="@color/slate_400"
                        android:textSize="24sp"
                        android:textStyle="bold" />

                    <EditText
                        android:id="@+id/etAmount"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="8dp"
                        android:background="@null"
                        android:fontFamily="sans-serif-medium"
                        android:inputType="numberDecimal"
                        android:text="1000"
                        android:textColor="@color/slate_900"
                        android:textSize="28sp"
                        android:textStyle="bold" />

                </LinearLayout>

                <!-- From Currency -->
                <androidx.constraintlayout.widget.ConstraintLayout
                    android:id="@+id/containerFrom"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="16dp"
                    android:background="@drawable/bg_input_rounded"
                    android:padding="12dp"
                    app:layout_constraintTop_toBottomOf="@id/amountContainer">

                    <ImageView
                        android:id="@+id/ivContentFrom"
                        android:layout_width="32dp"
                        android:layout_height="24dp"
                        android:scaleType="centerCrop"
                        android:src="@drawable/ic_launcher_background" 
                        app:layout_constraintBottom_toBottomOf="parent"
                        app:layout_constraintStart_toStartOf="parent"
                        app:layout_constraintTop_toTopOf="parent" />

                    <LinearLayout
                        android:id="@+id/textFromGroup"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="12dp"
                        android:orientation="vertical"
                        app:layout_constraintBottom_toBottomOf="parent"
                        app:layout_constraintStart_toEndOf="@id/ivContentFrom"
                        app:layout_constraintTop_toTopOf="parent">

                        <TextView
                            android:id="@+id/tvFromCode"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="USD"
                            android:textColor="@color/slate_900"
                            android:textSize="14sp"
                            android:textStyle="bold" />

                        <TextView
                            android:id="@+id/tvFromName"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="United States Dollar"
                            android:textColor="@color/slate_500"
                            android:textSize="12sp" />
                    </LinearLayout>
                   
                </androidx.constraintlayout.widget.ConstraintLayout>
                
                <!-- Spacer for Swap Button -->
                 <View
                    android:id="@+id/spacerSwap"
                    android:layout_width="match_parent"
                    android:layout_height="12dp"
                    app:layout_constraintTop_toBottomOf="@id/containerFrom"/>

                <!-- To Currency -->
                <androidx.constraintlayout.widget.ConstraintLayout
                    android:id="@+id/containerTo"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:background="@drawable/bg_input_rounded"
                    android:padding="12dp"
                    app:layout_constraintTop_toBottomOf="@id/spacerSwap">

                    <ImageView
                        android:id="@+id/ivContentTo"
                        android:layout_width="32dp"
                        android:layout_height="24dp"
                        android:scaleType="centerCrop"
                        android:src="@drawable/ic_launcher_background"
                        app:layout_constraintBottom_toBottomOf="parent"
                        app:layout_constraintStart_toStartOf="parent"
                        app:layout_constraintTop_toTopOf="parent" />

                    <LinearLayout
                        android:id="@+id/textToGroup"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="12dp"
                        android:orientation="vertical"
                        app:layout_constraintBottom_toBottomOf="parent"
                        app:layout_constraintStart_toEndOf="@id/ivContentTo"
                        app:layout_constraintTop_toTopOf="parent">

                        <TextView
                            android:id="@+id/tvToCode"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="EUR"
                            android:textColor="@color/slate_900"
                            android:textSize="14sp"
                            android:textStyle="bold" />

                        <TextView
                            android:id="@+id/tvToName"
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:text="Euro"
                            android:textColor="@color/slate_500"
                            android:textSize="12sp" />
                    </LinearLayout>
                </androidx.constraintlayout.widget.ConstraintLayout>
                
                <!-- Floating Swap Button -->
                <androidx.cardview.widget.CardView
                    android:id="@+id/btnSwap"
                    android:layout_width="40dp"
                    android:layout_height="40dp"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="4dp"
                    app:cardBackgroundColor="@color/primary"
                    app:layout_constraintTop_toBottomOf="@id/containerFrom"
                    app:layout_constraintBottom_toTopOf="@id/containerTo"
                    app:layout_constraintStart_toStartOf="parent"
                    app:layout_constraintEnd_toEndOf="parent">
                    
                    <ImageView
                        android:layout_width="24dp"
                        android:layout_height="24dp"
                        android:layout_gravity="center"
                        android:src="@drawable/ic_swap_vert"
                        app:tint="@color/white"/>
                        
                </androidx.cardview.widget.CardView>

                <!-- Convert Button -->
                <Button
                    android:id="@+id/btnConvert"
                    android:layout_width="match_parent"
                    android:layout_height="56dp"
                    android:layout_marginTop="24dp"
                    android:backgroundTint="@color/primary"
                    android:text="Convert"
                    android:textAllCaps="false"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    app:cornerRadius="12dp"
                    app:iconGravity="end"
                    app:layout_constraintTop_toBottomOf="@id/containerTo" />

            </androidx.constraintlayout.widget.ConstraintLayout>

            <!-- Result Card -->
            <androidx.constraintlayout.widget.ConstraintLayout
                android:id="@+id/cardResult"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="24dp"
                android:background="@drawable/bg_card_result"
                android:elevation="4dp"
                android:padding="24dp">

                <TextView
                    android:id="@+id/tvLabelTotal"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Total Estimated"
                    android:textColor="@color/slate_400"
                    android:textSize="14sp"
                    app:layout_constraintEnd_toEndOf="parent"
                    app:layout_constraintStart_toStartOf="parent"
                    app:layout_constraintTop_toTopOf="parent" />

                <TextView
                    android:id="@+id/tvResultValue"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="4dp"
                    android:text="€925.40"
                    android:textColor="@color/white"
                    android:textSize="48sp"
                    android:textStyle="bold"
                    app:layout_constraintEnd_toEndOf="parent"
                    app:layout_constraintStart_toStartOf="parent"
                    app:layout_constraintTop_toBottomOf="@id/tvLabelTotal" />

                <TextView
                    android:id="@+id/tvExchangeRate"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:background="@drawable/bg_frosted_center" 
                    android:paddingHorizontal="12dp"
                    android:paddingVertical="4dp"
                    android:text="1 USD = 0.9254 EUR"
                    android:textColor="@color/slate_200"
                    android:textSize="12sp"
                    app:layout_constraintEnd_toEndOf="parent"
                    app:layout_constraintStart_toStartOf="parent"
                    app:layout_constraintTop_toBottomOf="@id/tvResultValue" />

                <LinearLayout
                    android:id="@+id/buttonContainer"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="24dp"
                    android:orientation="horizontal"
                    android:weightSum="2"
                    app:layout_constraintTop_toBottomOf="@id/tvExchangeRate">

                    <Button
                        android:id="@+id/btnSave"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_marginEnd="8dp"
                        android:layout_weight="1"
                        android:backgroundTint="#33FFFFFF"
                        android:text="Save"
                        android:textAllCaps="false"
                        app:cornerRadius="12dp"
                        app:strokeColor="#1AFFFFFF"
                        app:strokeWidth="1dp" />

                    <Button
                        android:id="@+id/btnShare"
                        android:layout_width="0dp"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="8dp"
                        android:layout_weight="1"
                        android:backgroundTint="#33FFFFFF"
                        android:text="Share"
                        android:textAllCaps="false"
                        app:cornerRadius="12dp"
                        app:strokeColor="#1AFFFFFF"
                        app:strokeWidth="1dp" />
                </LinearLayout>
                
                 <TextView
                    android:id="@+id/tvTimestamp"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Mid-market exchange rate at 14:02 UTC"
                    android:textColor="@color/slate_500"
                    android:textSize="10sp"
                    android:layout_marginTop="16dp"
                    app:layout_constraintEnd_toEndOf="parent"
                    app:layout_constraintStart_toStartOf="parent"
                    app:layout_constraintBottom_toBottomOf="parent"
                    app:layout_constraintTop_toBottomOf="@+id/buttonContainer"
                    app:layout_constraintVertical_bias="1.0"
                     />


            </androidx.constraintlayout.widget.ConstraintLayout>

            <!-- History Section -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="32dp"
                android:orientation="vertical">



                <androidx.recyclerview.widget.RecyclerView
                    android:id="@+id/rvHistory"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="16dp"
                    android:nestedScrollingEnabled="false" />

            </LinearLayout>

        </LinearLayout>
    </ScrollView>

</androidx.constraintlayout.widget.ConstraintLayout>
```

### res\layout\activity_splash.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/bg_splash">

    <!-- Top Left Blur Blob -->
    <ImageView
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginLeft="-50dp"
        android:layout_marginTop="-50dp"
        android:src="@drawable/bg_blob_blue"
        android:alpha="0.5"
        app:layout_constraintDimensionRatio="1:1"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintWidth_percent="0.7" />

    <!-- Bottom Right Blur Blob -->
    <ImageView
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginRight="-50dp"
        android:layout_marginBottom="-50dp"
        android:src="@drawable/bg_blob_violet"
        android:alpha="0.5"
        app:layout_constraintDimensionRatio="1:1"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintWidth_percent="0.7" />

    <!-- Globe Container -->
    <androidx.constraintlayout.widget.ConstraintLayout
        android:id="@+id/globeContainer"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:layout_constraintBottom_toTopOf="@+id/tvTitle"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_chainStyle="packed">

        <!-- Globe Grid Background -->
        <ImageView
            android:layout_width="320dp"
            android:layout_height="320dp"
            android:src="@drawable/ic_globe_background"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintBottom_toBottomOf="parent"/>

        <!-- Constellation Overlay -->
        <ImageView
            android:layout_width="320dp"
            android:layout_height="320dp"
            android:src="@drawable/ic_constellation"
            android:alpha="0.8"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintBottom_toBottomOf="parent"/>

        <!-- Glow Ring -->
        <ImageView
            android:layout_width="124dp"
            android:layout_height="124dp"
            android:src="@drawable/ic_glow_ring"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

        <!-- Center Badge -->
        <FrameLayout
            android:layout_width="96dp"
            android:layout_height="96dp"
            android:background="@drawable/bg_frosted_center"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintTop_toTopOf="parent">

            <ImageView
                android:layout_width="48dp"
                android:layout_height="48dp"
                android:layout_gravity="center"
                android:src="@drawable/ic_currency_exchange"
                app:tint="@color/white" />
        </FrameLayout>

    </androidx.constraintlayout.widget.ConstraintLayout>

    <!-- Title -->
    <TextView
        android:id="@+id/tvTitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:fontFamily="sans-serif-black"
        android:gravity="center"
        android:letterSpacing="-0.02"
        android:text="CURRENCY\nCONVERTER"
        android:textColor="@color/white"
        android:textSize="40sp"
        android:textStyle="italic"
        app:layout_constraintBottom_toTopOf="@+id/tvSubtitle"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/globeContainer" />

    <!-- Subtitle Group -->
    <TextView
        android:id="@+id/tvSubtitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:fontFamily="sans-serif-medium"
        android:gravity="center"
        android:letterSpacing="0.3"
        android:text="SWIFT GLOBAL EXCHANGE"
        android:textColor="#E6FFFFFF"
        android:textSize="13sp"
        android:textAllCaps="true"
        app:layout_constraintBottom_toTopOf="@+id/dividerLine"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/tvTitle" />

    <!-- Divider Line -->
    <View
        android:id="@+id/dividerLine"
        android:layout_width="48dp"
        android:layout_height="2dp"
        android:layout_marginTop="8dp"
        android:background="@color/electric_blue"
        android:alpha="0.5"
        app:layout_constraintBottom_toTopOf="@+id/tvVersion"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/tvSubtitle" />

    <!-- Version Text -->
    <TextView
        android:id="@+id/tvVersion"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:fontFamily="sans-serif-medium"
        android:letterSpacing="0.1"
        android:text="V 2.0.4 PREMIUM"
        android:textColor="#66FFFFFF"
        android:textSize="11sp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/dividerLine"
        android:layout_marginBottom="48dp"/>
        
    <!-- Bottom line below version (from HTML design) is subtle -->
     <View
        android:layout_width="128dp"
        android:layout_height="4dp"
        android:layout_marginTop="8dp"
        android:background="#1AFFFFFF"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/tvVersion" />


</androidx.constraintlayout.widget.ConstraintLayout>
```

### res\layout\fragment_currency_selection_bottom_sheet.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@drawable/bg_bottom_sheet"
    android:id="@+id/bottomSheetContainer">

    <!-- Drag Handle -->
    <View
        android:layout_width="48dp"
        android:layout_height="6dp"
        android:layout_gravity="center_horizontal"
        android:layout_marginTop="12dp"
        android:layout_marginBottom="8dp"
        android:background="#e5e7eb" 
        android:backgroundTint="#e5e7eb"/> <!-- Hardcoded faint grey to match Tailwind 'gray-300' roughly -->

    <!-- Header -->
    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingHorizontal="16dp"
        android:paddingBottom="8dp">
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Select Currency"
            android:textSize="18sp"
            android:textStyle="bold"
            android:textColor="#111418"/>
        <TextView
            android:id="@+id/btnDone"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentEnd="true"
            android:text="Done"
            android:textColor="@color/primary"
            android:textSize="16sp"
            android:textStyle="bold"
            android:padding="4dp"/>
    </RelativeLayout>

    <!-- Search Bar -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingHorizontal="16dp"
        android:paddingTop="8dp"
        android:paddingBottom="16dp">
        
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="40dp"
            android:background="@drawable/bg_search_box"
            android:gravity="center_vertical"
            android:paddingHorizontal="12dp"
            android:orientation="horizontal">
            
            <ImageView
                android:layout_width="20dp"
                android:layout_height="20dp"
                android:src="@drawable/ic_search"
                android:tint="#9ca3af"/>
                
            <EditText
                android:id="@+id/etSearch"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="@null"
                android:hint="Search name or code..."
                android:textSize="14sp"
                android:textColor="#111418"
                android:paddingStart="8dp"
                android:maxLines="1"
                android:inputType="text"/>
        </LinearLayout>
    </LinearLayout>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvCurrencies"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clipToPadding="false"
        android:paddingBottom="20dp"/>

</LinearLayout>
```

### res\layout\item_currency.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:padding="12dp"
    android:background="?attr/selectableItemBackground"
    android:clickable="true"
    android:focusable="true">

    <androidx.cardview.widget.CardView
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:layout_marginEnd="12dp"
        app:cardCornerRadius="20dp"
        app:cardBackgroundColor="@android:color/transparent"
        app:cardElevation="0dp"
        app:strokeWidth="1dp"
        app:strokeColor="#e5e7eb">
        <ImageView
            android:id="@+id/ivFlag"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scaleType="centerCrop"/>
    </androidx.cardview.widget.CardView>

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical">
        <TextView
            android:id="@+id/tvCode"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="USD"
            android:textColor="#111418"
            android:textStyle="bold"
            android:textSize="16sp"/>
        <TextView
            android:id="@+id/tvName"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="United States Dollar"
            android:textColor="#6b7280"
            android:textSize="14sp"/>
    </LinearLayout>

    <ImageView
        android:id="@+id/ivCheck"
        android:layout_width="24dp"
        android:layout_height="24dp"
        android:src="@drawable/ic_check"
        android:tint="@color/primary"
        android:visibility="gone"/>

</LinearLayout>
```

### res\layout\item_currency_header.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/tvHeaderTitle"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:paddingStart="16dp"
    android:paddingEnd="16dp"
    android:paddingTop="12dp"
    android:paddingBottom="8dp"
    android:background="#f9fafb" 
    android:text="POPULAR"
    android:textSize="12sp"
    android:textColor="#6b7280"
    android:textStyle="bold"
    android:textAllCaps="true" />
```

### res\layout\item_history.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="12dp"
    android:background="@drawable/bg_card_rounded"
    android:gravity="center_vertical"
    android:orientation="horizontal"
    android:padding="16dp">

    <ImageView
        android:id="@+id/ivIcon"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:background="@drawable/bg_input_rounded"
        android:backgroundTint="#e0f2fe"
        android:padding="8dp"
        android:src="@drawable/ic_swap_vert" 
        android:tint="@color/primary"/>

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="16dp"
        android:layout_weight="1"
        android:orientation="vertical">

        <TextView
            android:id="@+id/tvConversionPair"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="500 USD → 395 GBP"
            android:textColor="@color/slate_900"
            android:textSize="14sp"
            android:textStyle="bold" />

        <TextView
            android:id="@+id/tvDate"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Today, 10:23 AM"
            android:textColor="@color/slate_500"
            android:textSize="12sp" />
    </LinearLayout>

    <ImageView
        android:id="@+id/btnShareHistory"
        android:layout_width="24dp"
        android:layout_height="24dp"
        android:src="@android:drawable/ic_menu_share"
        android:tint="@color/slate_400" />
        
    <ImageView
        android:id="@+id/btnDeleteHistory"
        android:layout_width="24dp"
        android:layout_height="24dp"
        android:layout_marginStart="16dp"
        android:src="@android:drawable/ic_menu_delete"
        android:tint="@color/slate_400" />

</LinearLayout>
```

## Value Resources (Strings, Colors, Themes)

### res\values\colors.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
    <color name="electric_blue">#00d4ff</color>
    <color name="deep_violet">#2e026d</color>
    <color name="tech_purple">#150050</color>
    <color name="dark_blue_vibrant">#001233</color>
    <color name="metallic_mid">#a5b4fc</color>

    <!-- New UI Colors -->
    <color name="primary">#137fec</color>
    <color name="primary_dark">#106cc9</color>
    <color name="background_light">#f6f7f8</color>
    <color name="background_dark">#101922</color>
    <color name="slate_900">#0f172a</color>
    <color name="slate_500">#64748b</color>
    <color name="slate_400">#94a3b8</color>
    <color name="slate_200">#e2e8f0</color>
    <color name="slate_100">#f1f5f9</color>
    <color name="slate_50">#f8fafc</color>
    <color name="green_400">#4ade80</color>
    <color name="success_bg">#dcfce7</color> <!-- green-50 -->
    <color name="success_text">#16a34a</color> <!-- green-600 -->
</resources>
```

### res\values\strings.xml

```xml
<resources>
    <string name="app_name">Currency Converter</string>
</resources>
```

### res\values\themes.xml

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <!-- Base application theme. -->
    <style name="Base.Theme.CurrencyConverter" parent="Theme.Material3.DayNight.NoActionBar">
        <!-- Customize your light theme here. -->
        <!-- <item name="colorPrimary">@color/my_light_primary</item> -->
    </style>

    <style name="Theme.CurrencyConverter" parent="Base.Theme.CurrencyConverter" />

    <style name="CustomBottomSheetDialogTheme" parent="Theme.Design.BottomSheetDialog">
        <item name="bottomSheetStyle">@style/CustomBottomSheetStyle</item>
    </style>

    <style name="CustomBottomSheetStyle" parent="Widget.Design.BottomSheet.Modal">
        <item name="android:background">@android:color/transparent</item>
    </style>
</resources>
```
