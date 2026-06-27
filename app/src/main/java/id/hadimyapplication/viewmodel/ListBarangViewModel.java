package id.hadimyapplication.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import id.hadimyapplication.data.entity.Barang;
import id.hadimyapplication.repository.BarangRepository;

public class ListBarangViewModel extends AndroidViewModel {

    private BarangRepository repository;
    
    private MutableLiveData<FilterParams> filterParams = new MutableLiveData<>(new FilterParams("Semua", "", "A-Z"));
    
    public LiveData<List<Barang>> barangList;

    public ListBarangViewModel(Application application) {
        super(application);
        repository = new BarangRepository(application);
        
        barangList = Transformations.switchMap(filterParams, params -> 
            repository.getBarangFiltered(params.kategori, params.searchQuery, params.sortOrder)
        );
    }

    public void setKategori(String kategori) {
        FilterParams current = filterParams.getValue();
        if (current != null) {
            current.kategori = kategori;
            filterParams.setValue(current);
        }
    }

    public void setSearchQuery(String query) {
        FilterParams current = filterParams.getValue();
        if (current != null) {
            current.searchQuery = query;
            filterParams.setValue(current);
        }
    }

    public void setSortOrder(String sortOrder) {
        FilterParams current = filterParams.getValue();
        if (current != null) {
            current.sortOrder = sortOrder;
            filterParams.setValue(current);
        }
    }

    public void delete(Barang barang) {
        repository.delete(barang);
    }

    static class FilterParams {
        String kategori;
        String searchQuery;
        String sortOrder;

        FilterParams(String kategori, String searchQuery, String sortOrder) {
            this.kategori = kategori;
            this.searchQuery = searchQuery;
            this.sortOrder = sortOrder;
        }
    }
}
