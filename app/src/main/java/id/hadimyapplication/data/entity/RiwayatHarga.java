package id.hadimyapplication.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "riwayat_harga",
        foreignKeys = @ForeignKey(entity = Barang.class,
                                  parentColumns = "id",
                                  childColumns = "id_barang",
                                  onDelete = ForeignKey.CASCADE))
public class RiwayatHarga {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "id_barang")
    public int idBarang;

    @ColumnInfo(name = "harga_lama")
    public int hargaLama;

    @ColumnInfo(name = "harga_baru")
    public int hargaBaru;

    @ColumnInfo(name = "tanggal")
    public long tanggal;

    public RiwayatHarga(int idBarang, int hargaLama, int hargaBaru, long tanggal) {
        this.idBarang = idBarang;
        this.hargaLama = hargaLama;
        this.hargaBaru = hargaBaru;
        this.tanggal = tanggal;
    }
}
