package com.badal.physverse3d;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {

    private EditText formulaInput;
    private TextView errorText;
    private FrameLayout graphContainer;
    private LinearLayout slidersContainer;
    private GraphView graphView;

    private String dependentName = "y";
    private String rhsExpr = "";
    private String independentVar = "x";
    private Map<String, Float> extraVars = new HashMap<>();

    private Handler animHandler = new Handler();
    private boolean animating = false;
    private long animStartTime = 0;
    private static final float TIME_DURATION = 10f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        formulaInput = findViewById(R.id.formula_input);
        errorText = findViewById(R.id.error_text);
        graphContainer = findViewById(R.id.graph_container);
        slidersContainer = findViewById(R.id.sliders_container);

        graphView = new GraphView(this);
        graphContainer.addView(graphView);

        Button plotButton = findViewById(R.id.plot_button);
        plotButton.setOnClickListener(v -> handlePlot());

        formulaInput.setText("h = v*t - 0.5*g*t^2");
        handlePlot();
    }

    private void handlePlot() {
        animating = false;
        animHandler.removeCallbacksAndMessages(null);
        errorText.setText("");
        slidersContainer.removeAllViews();
        extraVars.clear();

        String formula = formulaInput.getText().toString().trim();
        if (!formula.contains("=")) {
            errorText.setText("Formula e '=' thakte hobe. Example: y = x^2");
            return;
        }

        String[] parts = formula.split("=", 2);
        dependentName = parts[0].trim();
        rhsExpr = parts[1].trim();

        Set<String> vars;
        try {
            vars = MathParser.extractVariables(rhsExpr);
        } catch (Exception e) {
            errorText.setText("Parse error: " + e.getMessage());
            return;
        }

        if (vars.contains("t")) {
            independentVar = "t";
        } else if (vars.contains("x")) {
            independentVar = "x";
        } else if (!vars.isEmpty()) {
            independentVar = vars.iterator().next();
        } else {
            errorText.setText("Kono variable pawa jayni RHS e.");
            return;
        }

        for (String v : vars) {
            if (!v.equals(independentVar)) {
                extraVars.put(v, 1.0f);
                addSliderFor(v);
            }
        }

        recomputeCurve();

        if (independentVar.equals("t")) {
            startTimeAnimation();
        }
    }

    private void addSliderFor(String name) {
        TextView label = new TextView(this);
        label.setText(name + ": 1.0");
        label.setTextColor(0xFFFFFFFF);
        slidersContainer.addView(label);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(100);
        seekBar.setProgress(10);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        seekBar.setLayoutParams(lp);
        slidersContainer.addView(seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = Math.max(progress, 1) / 10f;
                label.setText(name + ": " + value);
                extraVars.put(name, value);
                recomputeCurve();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void recomputeCurve() {
        List<float[]> points = new ArrayList<>();
        float domainMin = independentVar.equals("t") ? 0f : -10f;
        float domainMax = independentVar.equals("t") ? TIME_DURATION : 10f;
        int steps = 200;

        MathParser parser = new MathParser(rhsExpr);

        for (int i = 0; i <= steps; i++) {
            float ind = domainMin + (domainMax - domainMin) * i / steps;
            Map<String, Double> vals = new HashMap<>();
            vals.put(independentVar, (double) ind);
            for (Map.Entry<String, Float> e : extraVars.entrySet()) {
                vals.put(e.getKey(), (double) e.getValue());
            }
            try {
                double result = parser.evaluate(vals);
                points.add(new float[]{ind, (float) result});
            } catch (Exception e) {
                errorText.setText("Eval error: " + e.getMessage());
                return;
            }
        }
        graphView.setData(points);
    }

    private void startTimeAnimation() {
        animating = true;
        animStartTime = System.currentTimeMillis();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (!animating) return;
                float elapsed = (System.currentTimeMillis() - animStartTime) / 1000f;
                float fraction = (elapsed % TIME_DURATION) / TIME_DURATION;
                float currentT = fraction * TIME_DURATION;
                MathParser parser = new MathParser(rhsExpr);
                Map<String, Double> vals = new HashMap<>();
                vals.put("t", (double) currentT);
                for (Map.Entry<String, Float> e : extraVars.entrySet()) {
                    vals.put(e.getKey(), (double) e.getValue());
                }
                String info;
                try {
                    double val = parser.evaluate(vals);
                    info = dependentName + " = " + String.format("%.2f", val) + "   t = " + String.format("%.2f", currentT);
                } catch (Exception e) {
                    info = "t = " + String.format("%.2f", currentT);
                }
                graphView.setMarkerFraction(fraction, info);
                animHandler.postDelayed(this, 50);
            }
        };
        animHandler.post(r);
    }

    @Override
    protected void onPause() {
        super.onPause();
        animating = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (independentVar.equals("t") && !rhsExpr.isEmpty()) {
            startTimeAnimation();
        }
    }
}
