package id.hadimyapplication;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

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

import id.hadimyapplication.adapter.RiwayatAdapter;
import id.hadimyapplication.data.dao.RiwayatHargaDao.RiwayatHargaWithBarang;
import id.hadimyapplication.databinding.ActivityRiwayatHargaBinding;
import id.hadimyapplication.viewmodel.RiwayatHargaViewModel;

public class RiwayatHargaActivity extends AppCompatActivity {

    private ActivityRiwayatHargaBinding binding;
    private RiwayatHargaViewModel viewModel;
    private RiwayatAdapter adapter;
    private List<RiwayatHargaWithBarang> currentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRiwayatHargaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RiwayatHargaViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupFab();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new RiwayatAdapter();
        binding.rvRiwayat.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRiwayat.setAdapter(adapter);
    }

    private void observeData() {
        viewModel.getAllRiwayat().observe(this, riwayatList -> {
            currentList = riwayatList;
            if (riwayatList != null) {
                adapter.setRiwayatList(riwayatList);
                if (riwayatList.isEmpty()) {
                    binding.rvRiwayat.setVisibility(View.GONE);
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    binding.rvRiwayat.setVisibility(View.VISIBLE);
                    binding.tvEmptyState.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupFab() {
        binding.fabExportPdf.setOnClickListener(v -> {
            if (currentList != null && !currentList.isEmpty()) {
                exportToPdf(currentList);
            } else {
                Toast.makeText(this, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exportToPdf(List<RiwayatHargaWithBarang> list) {
        Document document = new Document();
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "Riwayat_Harga_" + System.currentTimeMillis() + ".pdf";
            File file = new File(dir, fileName);

            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Paragraph header = new Paragraph("Laporan Riwayat Perubahan Harga Toko Ananda\n\n");
            document.add(header);

            PdfPTable table = new PdfPTable(4);
            table.addCell("Tanggal");
            table.addCell("Nama Barang");
            table.addCell("Harga Lama");
            table.addCell("Harga Baru");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("in", "ID"));
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

            for (RiwayatHargaWithBarang riwayat : list) {
                table.addCell(sdf.format(new Date(riwayat.tanggal)));
                table.addCell(riwayat.namaBarang);
                table.addCell(formatRupiah.format(riwayat.harga_lama));
                table.addCell(formatRupiah.format(riwayat.harga_baru));
            }

            document.add(table);
            
            SimpleDateFormat footerSdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("in", "ID"));
            document.add(new Paragraph("\nDicetak pada: " + footerSdf.format(new Date())));
            
            document.close();
            Toast.makeText(this, "PDF berhasil disimpan di folder Downloads", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal mengekspor PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
