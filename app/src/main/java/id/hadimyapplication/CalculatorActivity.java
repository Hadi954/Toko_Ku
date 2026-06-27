package id.hadimyapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class CalculatorActivity extends AppCompatActivity {

    private TextView tvDisplay;
    private StringBuilder input = new StringBuilder();
    private boolean justCalculated = false;

    private DecimalFormat df;
    private final NumberFormat rupiahFormat = NumberFormat.getNumberInstance(new Locale("in", "ID"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        df = new DecimalFormat("#.##########", symbols);

        tvDisplay = findViewById(R.id.tvDisplay);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        setNumericOnClickListener();
        setOperatorOnClickListener();
        updateDisplay(input.toString());
    }

    private void updateDisplay(String expression) {
        if (expression == null || expression.isEmpty()) {
            tvDisplay.setText("0");
            return;
        }

        StringBuilder formatted = new StringBuilder();
        StringBuilder currentNumber = new StringBuilder();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            // Allow negative sign at start or after an operator
            if (c == '-' && currentNumber.length() == 0 && (i == 0 || isOperator(expression.charAt(i - 1)))) {
                currentNumber.append(c);
            } else if (Character.isDigit(c) || c == '.') {
                currentNumber.append(c);
            } else {
                if (currentNumber.length() > 0) {
                    formatted.append(formatNumber(currentNumber.toString()));
                    currentNumber.setLength(0);
                }
                formatted.append(" ").append(c).append(" ");
            }
        }
        if (currentNumber.length() > 0) {
            formatted.append(formatNumber(currentNumber.toString()));
        }

        tvDisplay.setText(formatted.toString().trim());
    }

    private String formatNumber(String str) {
        if (str.equals(".")) return ".";
        if (str.equals("-")) return "-";
        if (str.startsWith("-.")) return "-.";
        
        boolean isNegative = str.startsWith("-");
        String numPart = isNegative ? str.substring(1) : str;
        
        try {
            if (numPart.contains(".")) {
                String[] parts = numPart.split("\\.");
                long integerPart = Long.parseLong(parts[0].isEmpty() ? "0" : parts[0]);
                String fmt = rupiahFormat.format(integerPart);
                if (parts.length > 1) return (isNegative ? "-" : "") + fmt + "," + parts[1];
                else return (isNegative ? "-" : "") + fmt + ",";
            } else {
                return (isNegative ? "-" : "") + rupiahFormat.format(Long.parseLong(numPart));
            }
        } catch (NumberFormatException e) {
            return str;
        }
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '×' || c == '÷';
    }

    private void setNumericOnClickListener() {
        View.OnClickListener listener = v -> {
            Button button = (Button) v;
            if (justCalculated) {
                input.setLength(0);
                justCalculated = false;
            }
            if (input.length() == 1 && input.charAt(0) == '0') {
                input.setLength(0);
            }
            input.append(button.getText().toString());
            updateDisplay(input.toString());
        };

        findViewById(R.id.btn0).setOnClickListener(listener);
        findViewById(R.id.btn1).setOnClickListener(listener);
        findViewById(R.id.btn2).setOnClickListener(listener);
        findViewById(R.id.btn3).setOnClickListener(listener);
        findViewById(R.id.btn4).setOnClickListener(listener);
        findViewById(R.id.btn5).setOnClickListener(listener);
        findViewById(R.id.btn6).setOnClickListener(listener);
        findViewById(R.id.btn7).setOnClickListener(listener);
        findViewById(R.id.btn8).setOnClickListener(listener);
        findViewById(R.id.btn9).setOnClickListener(listener);
        
        findViewById(R.id.btnDot).setOnClickListener(v -> {
            if (justCalculated) {
                input.setLength(0);
                justCalculated = false;
            }
            // Check if current number already has a dot
            int lastOpIdx = -1;
            for (int i = input.length() - 1; i >= 0; i--) {
                if (isOperator(input.charAt(i))) {
                    lastOpIdx = i;
                    break;
                }
            }
            String currentNum = input.substring(lastOpIdx + 1);
            if (!currentNum.contains(".")) {
                if (currentNum.isEmpty()) input.append("0");
                input.append(".");
                updateDisplay(input.toString());
            }
        });
    }

    private void setOperatorOnClickListener() {
        findViewById(R.id.btnAdd).setOnClickListener(v -> appendOperator('+'));
        findViewById(R.id.btnSubtract).setOnClickListener(v -> appendOperator('-'));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> appendOperator('×'));
        findViewById(R.id.btnDivide).setOnClickListener(v -> appendOperator('÷'));

        findViewById(R.id.btnPercent).setOnClickListener(v -> {
            if (input.length() > 0) {
                try {
                    double val = eval(input.toString().replace("×", "*").replace("÷", "/"));
                    val = val / 100.0;
                    input.setLength(0);
                    input.append(df.format(val));
                    updateDisplay(input.toString());
                    justCalculated = true;
                } catch (Exception e) {
                    Toast.makeText(this, "Format salah", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btnEquals).setOnClickListener(v -> {
            if (input.length() > 0) {
                try {
                    double result = eval(input.toString().replace("×", "*").replace("÷", "/"));
                    input.setLength(0);
                    input.append(df.format(result));
                    updateDisplay(input.toString());
                    justCalculated = true;
                } catch (Exception e) {
                    Toast.makeText(this, "Format salah", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btnClear).setOnClickListener(v -> {
            input.setLength(0);
            justCalculated = false;
            updateDisplay("0");
        });

        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            if (input.length() > 0) {
                input.deleteCharAt(input.length() - 1);
                updateDisplay(input.toString());
            }
        });
    }

    private void appendOperator(char op) {
        if (justCalculated) {
            justCalculated = false;
        }
        if (input.length() > 0) {
            char lastChar = input.charAt(input.length() - 1);
            if (isOperator(lastChar)) {
                input.setCharAt(input.length() - 1, op);
            } else {
                input.append(op);
            }
            updateDisplay(input.toString());
        } else if (op == '-') {
            input.append(op);
            updateDisplay(input.toString());
        }
    }

    // A simple math expression evaluator
    private double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }
                return x;
            }
        }.parse();
    }
}
