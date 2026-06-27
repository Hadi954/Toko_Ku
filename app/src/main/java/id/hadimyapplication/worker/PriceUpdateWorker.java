package id.hadimyapplication.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class PriceUpdateWorker extends Worker {

    public PriceUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        NotificationHelper.showNotification(
                getApplicationContext(),
                "Waktunya Cek Harga",
                "Jangan lupa cek dan update harga barang di Toko Ananda hari ini!"
        );
        return Result.success();
    }
}
