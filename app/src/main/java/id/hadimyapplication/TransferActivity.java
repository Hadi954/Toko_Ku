package id.hadimyapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.util.Locale;

public class TransferActivity extends AppCompatActivity {

    private EditText etNominal;
    private TextView tvAdminFee, tvTotal, tvTierInfo;
    private LinearLayout layoutTable;

    private final NumberFormat rupiah = NumberFormat.getNumberInstance(new Locale("in", "ID"));

    // Tier table: {maxNominal, adminFee} — mutable so user can edit
    private long[][] tiers = {
        {100_000L,   2_000L},
        {200_000L,   3_000L},
        {300_000L,   4_000L},
        {400_000L,   5_000L},
        {500_000L,   6_000L},
        {600_000L,   7_000L},
        {700_000L,   8_000L},
        {800_000L,   9_000L},
        {900_000L,  10_000L},
        {1_000_000L, 11_000L},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etNominal = findViewById(R.id.etNominal);
        tvAdminFee = findViewById(R.id.tvAdminFee);
        tvTotal = findViewById(R.id.tvTotal);
        tvTierInfo = findViewById(R.id.tvTierInfo);
        layoutTable = findViewById(R.id.layoutTable);

        buildTierTable();

        etNominal.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) { 
                if (isUpdating) return;
                isUpdating = true;

                String str = s.toString().replaceAll("[^\\d]", "");
                try {
                    if (!str.isEmpty()) {
                        long parsed = Long.parseLong(str);
                        String formatted = rupiah.format(parsed);
                        etNominal.setText(formatted);
                        etNominal.setSelection(formatted.length());
                    } else {
                        etNominal.setText("");
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }

                calculate(); 
                isUpdating = false;
            }
        });

        calculate(); // init state
    }

    private void calculate() {
        long nominal = getNominal();
        if (nominal <= 0) {
            tvAdminFee.setText("Rp 0");
            tvTotal.setText("Rp 0");
            tvTierInfo.setText("Masukkan nominal transfer");
            highlightTierRow(-1);
            return;
        }

        // Check if nominal exceeds the max tier
        long maxTier = tiers[tiers.length - 1][0];
        if (nominal > maxTier) {
            tvAdminFee.setText("—");
            tvTotal.setText("—");
            tvTierInfo.setText("Melebihi batas tarif (> Rp " + rupiah.format(maxTier) + ")");
            highlightTierRow(-1);
            return;
        }

        int tierIdx = getTierIndex(nominal);
        long fee = tiers[tierIdx][1];
        long total = nominal + fee;

        tvAdminFee.setText("Rp " + rupiah.format(fee));
        tvTotal.setText("Rp " + rupiah.format(total));
        tvTierInfo.setText("Masuk tier Rp " + rupiah.format(tierIdx == 0 ? 1 : tiers[tierIdx - 1][0] + 1)
                + " – Rp " + rupiah.format(tiers[tierIdx][0]));

        highlightTierRow(tierIdx);
    }

    private int getTierIndex(long nominal) {
        for (int i = 0; i < tiers.length; i++) {
            if (nominal <= tiers[i][0]) return i;
        }
        return tiers.length - 1;
    }

    private void highlightTierRow(int activeIndex) {
        for (int i = 0; i < layoutTable.getChildCount(); i++) {
            android.view.View row = layoutTable.getChildAt(i);
            if (i == 0) continue; // skip header
            int dataIndex = i - 1;
            if (dataIndex == activeIndex) {
                row.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_variant));
                setRowBold(row, true);
            } else {
                int bgColor = dataIndex % 2 == 0
                        ? ContextCompat.getColor(this, R.color.surface)
                        : android.graphics.Color.TRANSPARENT;
                row.setBackgroundColor(bgColor);
                setRowBold(row, false);
            }
        }
    }

    private void setRowBold(android.view.View row, boolean bold) {
        if (row instanceof LinearLayout) {
            LinearLayout ll = (LinearLayout) row;
            for (int i = 0; i < ll.getChildCount(); i++) {
                android.view.View v = ll.getChildAt(i);
                if (v instanceof TextView) {
                    ((TextView) v).setTypeface(null, bold
                            ? android.graphics.Typeface.BOLD
                            : android.graphics.Typeface.NORMAL);
                    ((TextView) v).setTextColor(bold
                            ? ContextCompat.getColor(this, R.color.secondary)
                            : ContextCompat.getColor(this, R.color.on_surface));
                }
            }
        }
    }

    private void buildTierTable() {
        layoutTable.removeAllViews();

        // Header row
        layoutTable.addView(makeHeaderRow());

        // Data rows
        for (int i = 0; i < tiers.length; i++) {
            long minNominal = i == 0 ? 1 : tiers[i - 1][0] + 1;
            long maxNominal = tiers[i][0];
            long fee = tiers[i][1];
            layoutTable.addView(makeDataRow(i, minNominal, maxNominal, fee));
        }
    }

    private android.view.View makeHeaderRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(16), dp(12), dp(16), dp(12));
        row.setBackgroundColor(ContextCompat.getColor(this, R.color.tertiary));

        TextView tvNominal = makeCell("Nominal Transfer", 0.6f, true);
        TextView tvFee = makeCell("Biaya Admin", 0.4f, true);

        row.addView(tvNominal);
        row.addView(tvFee);
        return row;
    }

    private android.view.View makeDataRow(int index, long min, long max, long fee) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(16), dp(10), dp(16), dp(10));
        int bgColor = index % 2 == 0
                ? ContextCompat.getColor(this, R.color.surface)
                : android.graphics.Color.TRANSPARENT;
        row.setBackgroundColor(bgColor);

        String nominalStr = "Rp " + rupiah.format(min) + " – Rp " + rupiah.format(max);
        String feeStr = "Rp " + rupiah.format(fee);

        TextView tvNominalCell = makeCell(nominalStr, 0.6f, false);
        TextView tvFeeCell = makeCell(feeStr, 0.4f, false);

        // Tap on nominal cell to edit the max nominal for this tier
        final int tierIndex = index;
        tvNominalCell.setBackground(ContextCompat.getDrawable(this, getSelectableItemBg()));
        tvNominalCell.setClickable(true);
        tvNominalCell.setFocusable(true);
        tvNominalCell.setOnClickListener(v -> showEditDialog(
                "Edit Nominal Tier " + (tierIndex + 1),
                tiers[tierIndex][0],
                newValue -> {
                    tiers[tierIndex][0] = newValue;
                    buildTierTable();
                    calculate();
                }
        ));

        // Tap on fee cell to edit the admin fee for this tier
        tvFeeCell.setBackground(ContextCompat.getDrawable(this, getSelectableItemBg()));
        tvFeeCell.setClickable(true);
        tvFeeCell.setFocusable(true);
        tvFeeCell.setOnClickListener(v -> showEditDialog(
                "Edit Biaya Admin Tier " + (tierIndex + 1),
                tiers[tierIndex][1],
                newValue -> {
                    tiers[tierIndex][1] = newValue;
                    buildTierTable();
                    calculate();
                }
        ));

        row.addView(tvNominalCell);
        row.addView(tvFeeCell);
        return row;
    }

    private int getSelectableItemBg() {
        android.util.TypedValue outValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        return outValue.resourceId;
    }

    private interface OnValueEditedListener {
        void onValueEdited(long newValue);
    }

    private void showEditDialog(String title, long currentValue, OnValueEditedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        // Create EditText
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextSize(20);
        input.setGravity(Gravity.CENTER);

        // Set current value
        if (currentValue > 0) {
            input.setText(rupiah.format(currentValue));
            input.setSelection(input.getText().length());
        }
        input.setHint("Masukkan nominal");

        // Add formatting TextWatcher
        input.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                isUpdating = true;

                String str = s.toString().replaceAll("[^\\d]", "");
                try {
                    if (!str.isEmpty()) {
                        long parsed = Long.parseLong(str);
                        String formatted = rupiah.format(parsed);
                        input.setText(formatted);
                        input.setSelection(formatted.length());
                    }
                } catch (NumberFormatException e) {
                    // Ignore
                }
                isUpdating = false;
            }
        });

        // Wrap EditText with padding
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(dp(24), dp(16), dp(24), dp(8));
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String raw = input.getText().toString().replaceAll("[^\\d]", "");
            long value = 0;
            if (!raw.isEmpty()) {
                try { value = Long.parseLong(raw); } catch (NumberFormatException e) { /* ignore */ }
            }
            listener.onValueEdited(value);
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Auto-show keyboard
        input.requestFocus();
        input.postDelayed(() -> {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(input, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }, 200);
    }

    private TextView makeCell(String text, float weight, boolean isHeader) {
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight));
        tv.setText(text);
        tv.setTextSize(isHeader ? 13 : 13);
        tv.setTypeface(null, isHeader ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tv.setTextColor(ContextCompat.getColor(this, isHeader ? R.color.text_secondary : R.color.on_surface));
        tv.setPadding(dp(4), dp(4), dp(4), dp(4));
        return tv;
    }

    private long getNominal() {
        String raw = etNominal.getText().toString().replaceAll("[^\\d]", "");
        if (raw.isEmpty()) return 0;
        try { return Long.parseLong(raw); } catch (NumberFormatException e) { return 0; }
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
