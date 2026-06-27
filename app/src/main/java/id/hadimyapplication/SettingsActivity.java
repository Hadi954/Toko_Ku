package id.hadimyapplication;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import id.hadimyapplication.data.AppDatabase;
import id.hadimyapplication.utils.ZipManager;
import id.hadimyapplication.worker.NotificationHelper;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences prefs;

    private ActivityResultLauncher<String> createFileLauncher;
    private ActivityResultLauncher<String[]> openFileLauncher;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                createFileLauncher.launch("Backup_TokoAnanda.zip");
            } else {
                Toast.makeText(this, "Permission dibutuhkan untuk backup", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == 1002) {
            if (grantResults.length > 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                openFileLauncher.launch(new String[]{"application/zip"});
            } else {
                Toast.makeText(this, "Permission dibutuhkan untuk restore", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("app_settings", MODE_PRIVATE);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        SwitchMaterial switchDarkMode = findViewById(R.id.switchDarkMode);
        MaterialButton btnBackup = findViewById(R.id.btnBackup);
        MaterialButton btnRestore = findViewById(R.id.btnRestore);
        switchDarkMode.setChecked(prefs.getBoolean("dark_mode", false));

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Register SAF launchers
        createFileLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/zip"),
                uri -> {
                    if (uri != null) {
                        performZipBackup(uri);
                    }
                });
                
        openFileLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        performZipRestore(uri);
                    }
                });

        btnBackup.setOnClickListener(v -> checkStoragePermissionAndLaunchBackup());
        btnRestore.setOnClickListener(v -> checkStoragePermissionAndLaunchRestore());
    }

    private void checkStoragePermissionAndLaunchBackup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createFileLauncher.launch("Backup_TokoAnanda.zip");
        } else {
            int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission == PermissionChecker.PERMISSION_GRANTED) {
                createFileLauncher.launch("Backup_TokoAnanda.zip");
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
            }
        }
    }

    private void checkStoragePermissionAndLaunchRestore() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            openFileLauncher.launch(new String[]{"application/zip"});
        } else {
            int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permission == PermissionChecker.PERMISSION_GRANTED) {
                openFileLauncher.launch(new String[]{"application/zip"});
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1002);
            }
        }
    }

    private void performZipBackup(Uri destUri) {
        Toast.makeText(this, "Memproses Backup...", Toast.LENGTH_SHORT).show();
        java.util.concurrent.Executor executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Ensure DB is fully written to disk before copying
                AppDatabase db = AppDatabase.getDatabase(this);
                db.close();

                File dbFile = getDatabasePath("toko_ananda_database");
                File dbWalFile = getDatabasePath("toko_ananda_database-wal");
                File dbShmFile = getDatabasePath("toko_ananda_database-shm");
                
                File imagesDir = new File(getFilesDir(), "images");

                File tempDir = new File(getCacheDir(), "backup_temp");
                if (tempDir.exists()) deleteRecursively(tempDir);
                tempDir.mkdirs();

                // Copy DB files to temp
                if (dbFile.exists()) copyFile(dbFile, new File(tempDir, dbFile.getName()));
                if (dbWalFile.exists()) copyFile(dbWalFile, new File(tempDir, dbWalFile.getName()));
                if (dbShmFile.exists()) copyFile(dbShmFile, new File(tempDir, dbShmFile.getName()));
                
                // Copy Images
                if (imagesDir.exists()) {
                    File tempImagesDir = new File(tempDir, "images");
                    tempImagesDir.mkdirs();
                    File[] images = imagesDir.listFiles();
                    if (images != null) {
                        for (File img : images) {
                            copyFile(img, new File(tempImagesDir, img.getName()));
                        }
                    }
                }

                // Zip everything in tempDir
                File zipFile = new File(getCacheDir(), "backup_toko_ananda.zip");
                if (zipFile.exists()) zipFile.delete();
                
                File[] filesToZip = tempDir.listFiles();
                if (filesToZip != null && filesToZip.length > 0) {
                    String[] filePaths = new String[filesToZip.length];
                    for (int i = 0; i < filesToZip.length; i++) {
                        filePaths[i] = filesToZip[i].getAbsolutePath();
                    }
                    ZipManager.zip(filePaths, zipFile.getAbsolutePath());

                    // Copy to destination URI
                    try (InputStream in = new FileInputStream(zipFile);
                         OutputStream out = getContentResolver().openOutputStream(destUri)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    }
                    
                    runOnUiThread(() -> Toast.makeText(this, "Backup ZIP Berhasil Disimpan!", Toast.LENGTH_LONG).show());
                }

                // Cleanup
                deleteRecursively(tempDir);
                if (zipFile.exists()) zipFile.delete();

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Backup Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void performZipRestore(Uri srcUri) {
        Toast.makeText(this, "Memproses Restore...", Toast.LENGTH_SHORT).show();
        java.util.concurrent.Executor executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Copy source URI to local temp zip
                File tempZip = new File(getCacheDir(), "restore_temp.zip");
                try (InputStream in = getContentResolver().openInputStream(srcUri);
                     OutputStream out = new FileOutputStream(tempZip)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }

                File extractDir = new File(getCacheDir(), "restore_extracted");
                if (extractDir.exists()) deleteRecursively(extractDir);
                extractDir.mkdirs();

                // Unzip
                ZipManager.unzip(tempZip.getAbsolutePath(), extractDir.getAbsolutePath());

                // Close DB before overwriting
                AppDatabase.getDatabase(this).close();

                File dbFile = getDatabasePath("toko_ananda_database");
                File dbWalFile = getDatabasePath("toko_ananda_database-wal");
                File dbShmFile = getDatabasePath("toko_ananda_database-shm");
                
                // Clear existing WAL and SHM
                if (dbWalFile.exists()) dbWalFile.delete();
                if (dbShmFile.exists()) dbShmFile.delete();

                // Replace DB
                File extDb = new File(extractDir, "toko_ananda_database");
                File extWal = new File(extractDir, "toko_ananda_database-wal");
                File extShm = new File(extractDir, "toko_ananda_database-shm");
                
                if (extDb.exists()) copyFile(extDb, dbFile);
                if (extWal.exists()) copyFile(extWal, dbWalFile);
                if (extShm.exists()) copyFile(extShm, dbShmFile);

                // Replace Images
                File imagesDir = new File(getFilesDir(), "images");
                if (imagesDir.exists()) deleteRecursively(imagesDir);
                imagesDir.mkdirs();

                File extImages = new File(extractDir, "images");
                if (extImages.exists() && extImages.isDirectory()) {
                    File[] images = extImages.listFiles();
                    if (images != null) {
                        for (File img : images) {
                            copyFile(img, new File(imagesDir, img.getName()));
                        }
                    }
                }

                // Cleanup
                deleteRecursively(extractDir);
                tempZip.delete();

                runOnUiThread(() -> {
                    Toast.makeText(this, "Restore Berhasil! Silakan restart aplikasi.", Toast.LENGTH_LONG).show();
                    // Force restart app
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Restore Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void copyFile(File source, File dest) throws Exception {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    private void deleteRecursively(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursively(child);
            }
        }
        fileOrDirectory.delete();
    }
}
