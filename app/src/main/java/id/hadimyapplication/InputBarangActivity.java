package id.hadimyapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import id.hadimyapplication.data.entity.Barang;
import id.hadimyapplication.databinding.ActivityInputBarangBinding;
import id.hadimyapplication.viewmodel.InputBarangViewModel;

public class InputBarangActivity extends AppCompatActivity {

    private ActivityInputBarangBinding binding;
    private InputBarangViewModel viewModel;
    private String selectedImagePath = "";
    private int barangId = -1;
    private Barang currentBarang = null;
    private Uri cameraImageUri = null;

    private final ActivityResultLauncher<Intent> pickImageFromGallery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        saveImageToInternalStorage(imageUri);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Uri> takePhoto = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && cameraImageUri != null) {
                    saveImageToInternalStorage(cameraImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInputBarangBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(InputBarangViewModel.class);

        setupToolbar();
        setupDropdown();
        setupListeners();

        if (getIntent().hasExtra("EXTRA_ID_BARANG")) {
            barangId = getIntent().getIntExtra("EXTRA_ID_BARANG", -1);
            binding.toolbar.setTitle("Edit Barang");
            binding.btnSimpan.setText("Update Barang");
            loadData(barangId);
        }
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDropdown() {
        String[] categories = new String[]{"Minuman", "Obat", "Makanan", "Snack", "Sembako"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        binding.etKategori.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnGaleri.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageFromGallery.launch(intent);
        });

        binding.btnKamera.setOnClickListener(v -> {
            File photoFile = new File(getCacheDir(), "temp_photo_" + System.currentTimeMillis() + ".jpg");
            cameraImageUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );
            takePhoto.launch(cameraImageUri);
        });

        binding.btnSimpan.setOnClickListener(v -> saveBarang());
    }

    private void loadData(int id) {
        viewModel.getBarangById(id).observe(this, barang -> {
            if (barang != null) {
                currentBarang = barang;
                binding.etNamaBarang.setText(barang.namaBarang);
                binding.etKategori.setText(barang.kategori, false);
                binding.etHarga.setText(String.valueOf(barang.harga));
                if (barang.deskripsi != null) {
                    binding.etDeskripsi.setText(barang.deskripsi);
                }
                
                selectedImagePath = barang.gambar;
                if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                    binding.llImagePlaceholder.setVisibility(View.GONE);
                    Glide.with(this)
                            .load(new File(selectedImagePath))
                            .centerCrop()
                            .into(binding.ivPreview);
                }
            }
        });
    }

    private void saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            // Generate filename
            String filename = "img_" + System.currentTimeMillis() + ".jpg";
            File directory = new File(getFilesDir(), "images");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            File file = new File(directory, filename);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos); // Compress 80%
            fos.flush();
            fos.close();
            
            selectedImagePath = file.getAbsolutePath();
            
            binding.llImagePlaceholder.setVisibility(View.GONE);
            Glide.with(this)
                    .load(file)
                    .centerCrop()
                    .into(binding.ivPreview);
                    
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveBarang() {
        String nama = binding.etNamaBarang.getText().toString().trim();
        String kategori = binding.etKategori.getText().toString().trim();
        String hargaStr = binding.etHarga.getText().toString().trim();
        String deskripsi = binding.etDeskripsi.getText().toString().trim();

        if (nama.isEmpty()) {
            binding.etNamaBarang.setError("Nama barang tidak boleh kosong");
            return;
        }
        if (kategori.isEmpty()) {
            binding.etKategori.setError("Kategori harus dipilih");
            return;
        }
        if (hargaStr.isEmpty()) {
            binding.etHarga.setError("Harga tidak boleh kosong");
            return;
        }

        int harga = Integer.parseInt(hargaStr);
        if (harga < 0) {
            binding.etHarga.setError("Harga harus positif");
            return;
        }

        long now = System.currentTimeMillis();

        if (barangId == -1) {
            // Insert
            Barang newBarang = new Barang(nama, kategori, harga, selectedImagePath, deskripsi, now, now);
            viewModel.insert(newBarang);
            Toast.makeText(this, "Barang berhasil disimpan", Toast.LENGTH_SHORT).show();
        } else {
            // Update
            if (currentBarang != null) {
                int hargaLama = currentBarang.harga;
                currentBarang.namaBarang = nama;
                currentBarang.kategori = kategori;
                currentBarang.harga = harga;
                currentBarang.gambar = selectedImagePath;
                currentBarang.deskripsi = deskripsi;
                currentBarang.updatedAt = now;
                
                viewModel.update(currentBarang, hargaLama);
                Toast.makeText(this, "Barang berhasil diupdate", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}
