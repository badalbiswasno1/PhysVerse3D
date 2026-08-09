package com.badal.physverse3d;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class GraphView extends View {

    private List<float[]> points = new ArrayList<>();
    private float minX, maxX, minY, maxY;
    private float markerFraction = -1f;
    private String infoText = "";

    private final Paint axisPaint = new Paint();
    private final Paint curvePaint = new Paint();
    private final Paint markerPaint = new Paint();
    private final Paint textPaint = new Paint();

    public GraphView(Context context) {
        super(context);
        axisPaint.setColor(Color.parseColor("#444444"));
        axisPaint.setStrokeWidth(2f);
        curvePaint.setColor(Color.parseColor("#4ECDC4"));
        curvePaint.setStrokeWidth(6f);
        curvePaint.setStyle(Paint.Style.STROKE);
        curvePaint.setAntiAlias(true);
        markerPaint.setColor(Color.parseColor("#FFD23F"));
        markerPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36f);
        textPaint.setAntiAlias(true);
    }

    public void setData(List<float[]> pts) {
        this.points = pts;
        minX = Float.MAX_VALUE; maxX = -Float.MAX_VALUE;
        minY = Float.MAX_VALUE; maxY = -Float.MAX_VALUE;
        for (float[] p : pts) {
            minX = Math.min(minX, p[0]);
            maxX = Math.max(maxX, p[0]);
            minY = Math.min(minY, p[1]);
            maxY = Math.max(maxY, p[1]);
        }
        if (minY == maxY) { minY -= 1; maxY += 1; }
        if (minX == maxX) { minX -= 1; maxX += 1; }
        invalidate();
    }

    public void setMarkerFraction(float f, String info) {
        this.markerFraction = f;
        this.infoText = info;
        invalidate();
    }

    private float toScreenX(float x, int w) {
        float pad = 60f;
        return pad + (x - minX) / (maxX - minX) * (w - 2 * pad);
    }

    private float toScreenY(float y, int h) {
        float pad = 60f;
        return h - pad - (y - minY) / (maxY - minY) * (h - 2 * pad);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (points.isEmpty() || w == 0 || h == 0) return;

        if (minX <= 0 && maxX >= 0) {
            float sx = toScreenX(0, w);
            canvas.drawLine(sx, 0, sx, h, axisPaint);
        }
        if (minY <= 0 && maxY >= 0) {
            float sy = toScreenY(0, h);
            canvas.drawLine(0, sy, w, sy, axisPaint);
        }

        Path path = new Path();
        for (int i = 0; i < points.size(); i++) {
            float[] p = points.get(i);
            float sx = toScreenX(p[0], w);
            float sy = toScreenY(p[1], h);
            if (i == 0) path.moveTo(sx, sy); else path.lineTo(sx, sy);
        }
        canvas.drawPath(path, curvePaint);

        if (markerFraction >= 0f && !points.isEmpty()) {
            int idx = Math.min(points.size() - 1, (int) (markerFraction * (points.size() - 1)));
            float[] p = points.get(idx);
            float sx = toScreenX(p[0], w);
            float sy = toScreenY(p[1], h);
            canvas.drawCircle(sx, sy, 16f, markerPaint);
        }

        if (!infoText.isEmpty()) {
            canvas.drawText(infoText, 30, 50, textPaint);
        }
    }
}
