package id.hadimyapplication.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import id.hadimyapplication.data.entity.Barang;
import id.hadimyapplication.repository.BarangRepository;

public class InputBarangViewModel extends AndroidViewModel {

    private BarangRepository repository;

    public InputBarangViewModel(Application application) {
        super(application);
        repository = new BarangRepository(application);
    }

    public LiveData<Barang> getBarangById(int id) {
        return repository.getBarangById(id);
    }

    public void insert(Barang barang) {
        repository.insert(barang);
    }

    public void update(Barang barang, int hargaLama) {
        repository.update(barang, hargaLama);
    }
}
