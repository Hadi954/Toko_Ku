package id.hadimyapplication.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.hadimyapplication.data.dao.RiwayatHargaDao.RiwayatHargaWithBarang;
import id.hadimyapplication.databinding.ItemRiwayatBinding;

public class RiwayatAdapter extends RecyclerView.Adapter<RiwayatAdapter.RiwayatViewHolder> {

    private List<RiwayatHargaWithBarang> riwayatList = new ArrayList<>();

    public void setRiwayatList(List<RiwayatHargaWithBarang> riwayatList) {
        this.riwayatList = riwayatList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RiwayatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRiwayatBinding binding = ItemRiwayatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RiwayatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RiwayatViewHolder holder, int position) {
        holder.bind(riwayatList.get(position));
    }

    @Override
    public int getItemCount() {
        return riwayatList.size();
    }

    class RiwayatViewHolder extends RecyclerView.ViewHolder {
        private ItemRiwayatBinding binding;

        public RiwayatViewHolder(ItemRiwayatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RiwayatHargaWithBarang riwayat) {
            binding.tvNamaBarang.setText(riwayat.namaBarang);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("in", "ID"));
            binding.tvTanggal.setText(sdf.format(new Date(riwayat.tanggal)));

            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            binding.tvHargaLama.setText(formatRupiah.format(riwayat.harga_lama));
            binding.tvHargaLama.setPaintFlags(binding.tvHargaLama.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            
            binding.tvHargaBaru.setText(formatRupiah.format(riwayat.harga_baru));
        }
    }
}
