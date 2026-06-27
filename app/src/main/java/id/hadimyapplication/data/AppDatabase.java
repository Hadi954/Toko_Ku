package id.hadimyapplication.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import id.hadimyapplication.data.dao.BarangDao;
import id.hadimyapplication.data.dao.RiwayatHargaDao;
import id.hadimyapplication.data.entity.Barang;
import id.hadimyapplication.data.entity.RiwayatHarga;

@Database(entities = {Barang.class, RiwayatHarga.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract BarangDao barangDao();
    public abstract RiwayatHargaDao riwayatHargaDao();

    private static volatile AppDatabase INSTANCE;
    
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE barang ADD COLUMN deskripsi TEXT");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "toko_ananda_database")
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
