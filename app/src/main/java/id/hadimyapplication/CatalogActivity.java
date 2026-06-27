package id.hadimyapplication;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import id.hadimyapplication.adapter.CatalogPagerAdapter;
import id.hadimyapplication.databinding.ActivityCatalogBinding;
import id.hadimyapplication.viewmodel.MainViewModel;

public class CatalogActivity extends AppCompatActivity {

    private ActivityCatalogBinding binding;
    private CatalogPagerAdapter adapter;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make activity fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivityCatalogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new CatalogPagerAdapter();
        binding.rvCatalog.setAdapter(adapter);

        // Fetch data
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getSemuaBarang().observe(this, barangList -> {
            if (barangList != null && !barangList.isEmpty()) {
                adapter.setBarangList(barangList);
                binding.tvEmpty.setVisibility(View.GONE);
            } else {
                binding.tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        binding.btnClose.setOnClickListener(v -> finish());
    }
}
