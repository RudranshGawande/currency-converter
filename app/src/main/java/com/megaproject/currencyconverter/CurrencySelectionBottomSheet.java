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
