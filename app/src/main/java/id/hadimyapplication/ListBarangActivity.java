package id.hadimyapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;

import id.hadimyapplication.adapter.BarangAdapter;
import id.hadimyapplication.data.entity.Barang;
import id.hadimyapplication.databinding.ActivityListBarangBinding;
import id.hadimyapplication.viewmodel.ListBarangViewModel;

public class ListBarangActivity extends AppCompatActivity {

    private ActivityListBarangBinding binding;
    private ListBarangViewModel viewModel;
    private BarangAdapter adapter;
    private List<Barang> currentList;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListBarangBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ListBarangViewModel.class);
        
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupFab();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.inflateMenu(R.menu.menu_list_barang);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.sort_az) {
                viewModel.setSortOrder("A-Z");
                return true;
            } else if (id == R.id.sort_za) {
                viewModel.setSortOrder("Z-A");
                return true;
            } else if (id == R.id.sort_harga_rendah) {
                viewModel.setSortOrder("Harga Terendah");
                return true;
            } else if (id == R.id.sort_harga_tinggi) {
                viewModel.setSortOrder("Harga Tertinggi");
                return true;
            } else if (id == R.id.action_export_pdf) {
                if (currentList != null && !currentList.isEmpty()) {
                    exportToPdf(currentList);
                } else {
                    Toast.makeText(this, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new BarangAdapter();
        binding.rvBarang.setLayoutManager(new LinearLayoutManager(this));
        binding.rvBarang.setAdapter(adapter);

        adapter.setOnItemClickListener(new BarangAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Barang barang) {
                // Navigate to Detail or Edit (we will use InputBarangActivity for edit)
                Intent intent = new Intent(ListBarangActivity.this, InputBarangActivity.class);
                intent.putExtra("EXTRA_ID_BARANG", barang.id);
                startActivity(intent);
            }

            @Override
            public void onLongClick(Barang barang) {
                new AlertDialog.Builder(ListBarangActivity.this)
                        .setTitle("Hapus Barang")
                        .setMessage("Apakah Anda yakin ingin menghapus " + barang.namaBarang + "?")
                        .setPositiveButton("Ya", (dialog, which) -> {
                            viewModel.delete(barang);
                            Toast.makeText(ListBarangActivity.this, "Barang dihapus", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Tidak", null)
                        .show();
            }
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> viewModel.setSearchQuery(s.toString().trim());
                searchHandler.postDelayed(searchRunnable, 300); // 300ms debounce
            }
        });
    }

    private void setupFilters() {
        binding.chipGroupKategori.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                Chip chip = findViewById(id);
                if (chip != null) {
                    viewModel.setKategori(chip.getText().toString());
                }
            } else {
                viewModel.setKategori("Semua");
                binding.chipSemua.setChecked(true);
            }
        });
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, InputBarangActivity.class);
            startActivity(intent);
        });
    }

    private void observeData() {
        viewModel.barangList.observe(this, barangs -> {
            if (barangs != null) {
                currentList = barangs;
                adapter.setBarangList(barangs);
                if (barangs.isEmpty()) {
                    binding.rvBarang.setVisibility(View.GONE);
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    binding.rvBarang.setVisibility(View.VISIBLE);
                    binding.tvEmptyState.setVisibility(View.GONE);
                }
            }
        });
    }

    private void exportToPdf(List<Barang> list) {
        Document document = new Document();
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "Daftar_Barang_Toko_Ananda_" + System.currentTimeMillis() + ".pdf";
            File file = new File(dir, fileName);

            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Paragraph header = new Paragraph("Daftar Harga Barang Toko Ananda\n\n");
            document.add(header);

            PdfPTable table = new PdfPTable(4);
            table.addCell("No");
            table.addCell("Nama");
            table.addCell("Kategori");
            table.addCell("Harga");

            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

            int no = 1;
            for (Barang barang : list) {
                table.addCell(String.valueOf(no++));
                table.addCell(barang.namaBarang);
                table.addCell(barang.kategori);
                table.addCell(formatRupiah.format(barang.harga));
            }

            document.add(table);
            
            SimpleDateFormat footerSdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("in", "ID"));
            document.add(new Paragraph("\nDicetak pada: " + footerSdf.format(new Date())));
            
            document.close();
            Toast.makeText(this, "PDF Daftar Barang disimpan di folder Downloads", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal mengekspor PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
