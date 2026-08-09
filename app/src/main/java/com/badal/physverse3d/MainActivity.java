package com.badal.physverse3d;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private GLSurfaceView glSurfaceView;
    private CubeRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        renderer = new CubeRenderer();
        glSurfaceView.setRenderer(renderer);

        FrameLayout container = findViewById(R.id.gl_container);
        container.addView(glSurfaceView);

        setupSlider(R.id.seek_length, R.id.label_length, "দৈর্ঘ্য", (v) -> renderer.setLength(v));
        setupSlider(R.id.seek_width, R.id.label_width, "প্রস্থ", (v) -> renderer.setWidth(v));
        setupSlider(R.id.seek_height, R.id.label_height, "উচ্চতা", (v) -> renderer.setHeight(v));

        SeekBar seekTime = findViewById(R.id.seek_time);
        TextView labelTime = findViewById(R.id.label_time);
        seekTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float t = progress / 10f;
                labelTime.setText("সময়: " + t + " s");
                renderer.setTime(t);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    interface ValueListener {
        void onChange(float value);
    }

    private void setupSlider(int seekId, int labelId, String labelPrefix, ValueListener listener) {
        SeekBar seekBar = findViewById(seekId);
        TextView label = findViewById(labelId);
        seekBar.setProgress(10);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = Math.max(progress, 1) / 10f;
                label.setText(labelPrefix + ": " + value);
                listener.onChange(value);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}
