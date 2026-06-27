package id.hadimyapplication.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.OnConflictStrategy;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import id.hadimyapplication.data.entity.Barang;

@Dao
public interface BarangDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Barang barang);

    @Update
    void update(Barang barang);

    @Delete
    void delete(Barang barang);

    @Query("SELECT * FROM barang ORDER BY created_at DESC")
    LiveData<List<Barang>> getAllBarang();

    @Query("DELETE FROM barang")
    void deleteAllBarang();

    @Query("SELECT * FROM barang")
    List<Barang> getAllBarangSync();

    @Query("SELECT * FROM barang WHERE id = :id LIMIT 1")
    LiveData<Barang> getBarangById(int id);

    @Query("SELECT * FROM barang WHERE " +
           "(:kategori IS NULL OR :kategori = 'Semua' OR kategori = :kategori) AND " +
           "(:searchQuery IS NULL OR nama_barang LIKE '%' || :searchQuery || '%') " +
           "ORDER BY " +
           "CASE WHEN :sortOrder = 'A-Z' THEN nama_barang END ASC, " +
           "CASE WHEN :sortOrder = 'Z-A' THEN nama_barang END DESC, " +
           "CASE WHEN :sortOrder = 'Harga Terendah' THEN harga END ASC, " +
           "CASE WHEN :sortOrder = 'Harga Tertinggi' THEN harga END DESC, " +
           "created_at DESC")
    LiveData<List<Barang>> getBarangFiltered(String kategori, String searchQuery, String sortOrder);

    @Query("SELECT COUNT(*) FROM barang")
    LiveData<Integer> getTotalBarang();

    @Query("SELECT COUNT(DISTINCT kategori) FROM barang")
    LiveData<Integer> getTotalKategori();

    @Query("SELECT COUNT(*) FROM barang WHERE created_at >= :timestamp")
    LiveData<Integer> getBarangBaruCount(long timestamp);
}
