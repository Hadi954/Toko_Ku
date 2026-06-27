package id.hadimyapplication.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import id.hadimyapplication.data.entity.RiwayatHarga;

@Dao
public interface RiwayatHargaDao {

    @Insert
    void insert(RiwayatHarga riwayatHarga);

    @Query("SELECT r.*, b.nama_barang as namaBarang FROM riwayat_harga r INNER JOIN barang b ON r.id_barang = b.id ORDER BY r.tanggal DESC")
    LiveData<List<RiwayatHargaWithBarang>> getAllRiwayat();

    @Query("DELETE FROM riwayat_harga")
    void deleteAllRiwayat();

    @Query("SELECT * FROM riwayat_harga")
    List<RiwayatHarga> getAllRiwayatSync();

    @Query("SELECT r.*, b.nama_barang as namaBarang FROM riwayat_harga r INNER JOIN barang b ON r.id_barang = b.id WHERE r.id_barang = :idBarang ORDER BY r.tanggal DESC")
    LiveData<List<RiwayatHargaWithBarang>> getRiwayatByBarang(int idBarang);

    public static class RiwayatHargaWithBarang {
        public int id;
        public int id_barang;
        public int harga_lama;
        public int harga_baru;
        public long tanggal;
        public String namaBarang;
    }
}
