package id.hadimyapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class QrisActivity extends AppCompatActivity {

    private androidx.activity.result.ActivityResultLauncher<String> pickImageLauncher;
    private android.widget.ImageView ivQris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qris);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ivQris = findViewById(R.id.ivQris);
        MaterialButton btnShare = findViewById(R.id.btnShare);
        MaterialButton btnChangeQris = findViewById(R.id.btnChangeQris);
        MaterialButton btnResetQris = findViewById(R.id.btnResetQris);
        
        android.content.SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String qrisPath = prefs.getString("qris_image_path", null);
        if (qrisPath != null) {
            java.io.File imgFile = new java.io.File(qrisPath);
            if (imgFile.exists()) {
                ivQris.setImageURI(android.net.Uri.fromFile(imgFile));
            } else {
                ivQris.setImageDrawable(null);
                prefs.edit().remove("qris_image_path").apply();
            }
        }

        pickImageLauncher = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                try {
                    java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                    java.io.File internalFile = new java.io.File(getFilesDir(), "custom_qris.jpg");
                    java.io.FileOutputStream outputStream = new java.io.FileOutputStream(internalFile);
                    
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                    
                    outputStream.close();
                    inputStream.close();
                    
                    prefs.edit().putString("qris_image_path", internalFile.getAbsolutePath()).apply();
                    ivQris.setImageURI(android.net.Uri.fromFile(internalFile));
                } catch (Exception e) {
                    android.widget.Toast.makeText(this, "Gagal memuat gambar", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnChangeQris.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnResetQris.setOnClickListener(v -> {
            prefs.edit().remove("qris_image_path").apply();
            ivQris.setImageDrawable(null);
            java.io.File internalFile = new java.io.File(getFilesDir(), "custom_qris.jpg");
            if (internalFile.exists()) {
                internalFile.delete();
            }
            android.widget.Toast.makeText(this, "Foto QRIS dihapus", android.widget.Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            // Placeholder for share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "QRIS Toko Ananda");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Scan QRIS ini untuk pembayaran di Toko Ananda.");
            startActivity(Intent.createChooser(shareIntent, "Bagikan QRIS via"));
        });
    }
}
