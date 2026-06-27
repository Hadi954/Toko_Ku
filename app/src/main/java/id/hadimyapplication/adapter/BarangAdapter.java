package id.hadimyapplication.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.hadimyapplication.data.entity.Barang;
import id.hadimyapplication.databinding.ItemBarangBinding;

public class BarangAdapter extends RecyclerView.Adapter<BarangAdapter.BarangViewHolder> {

    private List<Barang> barangList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Barang barang);
        void onLongClick(Barang barang);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setBarangList(List<Barang> barangs) {
        this.barangList = barangs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BarangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBarangBinding binding = ItemBarangBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BarangViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BarangViewHolder holder, int position) {
        Barang currentBarang = barangList.get(position);
        holder.bind(currentBarang);
    }

    @Override
    public int getItemCount() {
        return barangList.size();
    }

    class BarangViewHolder extends RecyclerView.ViewHolder {
        private ItemBarangBinding binding;

        public BarangViewHolder(ItemBarangBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(barangList.get(position));
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onLongClick(barangList.get(position));
                    return true;
                }
                return false;
            });
        }

        public void bind(Barang barang) {
            binding.tvNamaBarang.setText(barang.namaBarang);
            binding.tvKategoriBadge.setText(barang.kategori);
            
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            binding.tvHargaBarang.setText(formatRupiah.format(barang.harga));

            if (barang.gambar != null && !barang.gambar.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(new File(barang.gambar))
                        .centerCrop()
                        .into(binding.ivGambarBarang);
            } else {
                binding.ivGambarBarang.setImageDrawable(null);
                binding.ivGambarBarang.setBackgroundColor(0xFFE0E0E0); // placeholder
            }
        }
    }
}
