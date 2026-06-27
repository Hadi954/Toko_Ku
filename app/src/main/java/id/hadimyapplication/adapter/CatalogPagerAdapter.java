package id.hadimyapplication.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.hadimyapplication.R;
import id.hadimyapplication.data.entity.Barang;

public class CatalogPagerAdapter extends RecyclerView.Adapter<CatalogPagerAdapter.CatalogViewHolder> {

    private List<Barang> barangList = new ArrayList<>();

    public void setBarangList(List<Barang> list) {
        this.barangList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CatalogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_catalog_page, parent, false);
        return new CatalogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CatalogViewHolder holder, int position) {
        Barang barang = barangList.get(position);
        holder.bind(barang);
    }

    @Override
    public int getItemCount() {
        return barangList.size();
    }

    static class CatalogViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCatalogImage;
        TextView tvCatalogName;
        TextView tvCatalogPrice;
        TextView tvCatalogCategory;
        TextView tvCatalogDesc;

        public CatalogViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCatalogImage = itemView.findViewById(R.id.ivCatalogImage);
            tvCatalogName = itemView.findViewById(R.id.tvCatalogName);
            tvCatalogPrice = itemView.findViewById(R.id.tvCatalogPrice);
            tvCatalogCategory = itemView.findViewById(R.id.tvCatalogCategory);
            tvCatalogDesc = itemView.findViewById(R.id.tvCatalogDesc);
        }

        public void bind(Barang barang) {
            tvCatalogName.setText(barang.namaBarang);
            tvCatalogCategory.setText(barang.kategori);

            if (barang.deskripsi != null && !barang.deskripsi.isEmpty()) {
                tvCatalogDesc.setVisibility(View.VISIBLE);
                tvCatalogDesc.setText(barang.deskripsi);
            } else {
                tvCatalogDesc.setVisibility(View.GONE);
            }

            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            tvCatalogPrice.setText(formatRupiah.format(barang.harga));

            if (barang.gambar != null && !barang.gambar.isEmpty()) {
                ivCatalogImage.setImageURI(Uri.parse(barang.gambar));
            } else {
                ivCatalogImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }
}
