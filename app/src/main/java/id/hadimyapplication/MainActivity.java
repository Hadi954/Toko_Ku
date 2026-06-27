package id.hadimyapplication;

import android.os.Bundle;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import id.hadimyapplication.databinding.ActivityMainBinding;
import id.hadimyapplication.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android.content.SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getTotalBarang().observe(this, total -> {
            binding.tvTotalBarang.setText(total != null ? String.valueOf(total) : "0");
        });

        viewModel.getTotalKategori().observe(this, kategori -> {
            binding.tvTotalKategori.setText(kategori != null ? String.valueOf(kategori) : "0");
        });

        viewModel.getBarangBaruCount().observe(this, baru -> {
            binding.tvBarangBaru.setText(baru != null ? String.valueOf(baru) : "0");
        });
    }

    private void setupListeners() {
        binding.btnKatalogPintar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CatalogActivity.class);
            startActivity(intent);
        });

        binding.btnKelolaBarang.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListBarangActivity.class);
            startActivity(intent);
        });

        binding.btnKalkulator.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalculatorActivity.class);
            startActivity(intent);
        });

        binding.btnTransfer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TransferActivity.class);
            startActivity(intent);
        });

        binding.btnQris.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QrisActivity.class);
            startActivity(intent);
        });

        binding.btnPengaturan.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}