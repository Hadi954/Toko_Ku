package id.hadimyapplication.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import id.hadimyapplication.repository.BarangRepository;

public class MainViewModel extends AndroidViewModel {

    private BarangRepository repository;

    public MainViewModel(Application application) {
        super(application);
        repository = new BarangRepository(application);
    }

    public LiveData<Integer> getTotalBarang() {
        return repository.getTotalBarang();
    }

    public LiveData<java.util.List<id.hadimyapplication.data.entity.Barang>> getSemuaBarang() {
        return repository.getAllBarang();
    }

    public LiveData<Integer> getTotalKategori() {
        return repository.getTotalKategori();
    }

    public LiveData<Integer> getBarangBaruCount() {
        // Last 7 days timestamp
        long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
        return repository.getBarangBaruCount(sevenDaysAgo);
    }
}
