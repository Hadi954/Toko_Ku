package id.hadimyapplication.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import id.hadimyapplication.data.AppDatabase;
import id.hadimyapplication.data.dao.BarangDao;
import id.hadimyapplication.data.dao.RiwayatHargaDao;
import id.hadimyapplication.data.entity.Barang;
import id.hadimyapplication.data.entity.RiwayatHarga;

public class BarangRepository {

    private BarangDao barangDao;
    private RiwayatHargaDao riwayatHargaDao;
    private LiveData<List<Barang>> allBarang;
    private ExecutorService executorService;

    public BarangRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        barangDao = db.barangDao();
        riwayatHargaDao = db.riwayatHargaDao();
        allBarang = barangDao.getAllBarang();
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<Barang>> getAllBarang() {
        return allBarang;
    }

    public LiveData<List<Barang>> getBarangFiltered(String kategori, String searchQuery, String sortOrder) {
        return barangDao.getBarangFiltered(kategori, searchQuery, sortOrder);
    }

    public LiveData<Barang> getBarangById(int id) {
        return barangDao.getBarangById(id);
    }

    public LiveData<Integer> getTotalBarang() {
        return barangDao.getTotalBarang();
    }

    public LiveData<Integer> getTotalKategori() {
        return barangDao.getTotalKategori();
    }

    public LiveData<Integer> getBarangBaruCount(long timestamp) {
        return barangDao.getBarangBaruCount(timestamp);
    }

    public void insert(Barang barang) {
        executorService.execute(() -> {
            barangDao.insert(barang);
        });
    }

    public void update(Barang barang, int hargaLama) {
        executorService.execute(() -> {
            barangDao.update(barang);
            // Record history if price changed
            if (barang.harga != hargaLama) {
                RiwayatHarga riwayat = new RiwayatHarga(barang.id, hargaLama, barang.harga, System.currentTimeMillis());
                riwayatHargaDao.insert(riwayat);
            }
        });
    }

    public void delete(Barang barang) {
        executorService.execute(() -> {
            barangDao.delete(barang);
        });
    }
}
