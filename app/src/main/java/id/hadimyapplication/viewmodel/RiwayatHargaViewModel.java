package id.hadimyapplication.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import id.hadimyapplication.data.AppDatabase;
import id.hadimyapplication.data.dao.RiwayatHargaDao;

public class RiwayatHargaViewModel extends AndroidViewModel {

    private RiwayatHargaDao riwayatHargaDao;
    private LiveData<List<RiwayatHargaDao.RiwayatHargaWithBarang>> allRiwayat;

    public RiwayatHargaViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        riwayatHargaDao = db.riwayatHargaDao();
        allRiwayat = riwayatHargaDao.getAllRiwayat();
    }

    public LiveData<List<RiwayatHargaDao.RiwayatHargaWithBarang>> getAllRiwayat() {
        return allRiwayat;
    }
}
