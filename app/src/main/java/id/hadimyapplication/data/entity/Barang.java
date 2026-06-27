package id.hadimyapplication.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "barang")
public class Barang {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "nama_barang")
    public String namaBarang;

    @ColumnInfo(name = "kategori")
    public String kategori;

    @ColumnInfo(name = "harga")
    public int harga;

    @ColumnInfo(name = "gambar")
    public String gambar;

    @ColumnInfo(name = "deskripsi")
    public String deskripsi;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    public Barang(String namaBarang, String kategori, int harga, String gambar, String deskripsi, long createdAt, long updatedAt) {
        this.namaBarang = namaBarang;
        this.kategori = kategori;
        this.harga = harga;
        this.gambar = gambar;
        this.deskripsi = deskripsi;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
