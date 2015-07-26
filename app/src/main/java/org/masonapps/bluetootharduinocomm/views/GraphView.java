package org.masonapps.bluetootharduinocomm.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import org.masonapps.bluetootharduinocomm.R;

/**
 * TODO: document your custom view class.
 */
public class GraphView extends View {
    private static final int DEFAULT_NUM_READINGS = 30;
    private static final int DEFAULT_GRAPH_COLOR = Color.RED;
    private static final int DEFAULT_LINE_COLOR = Color.GRAY;
    private static final float DEFAULT_MAX = 20f;
    private static final float DEFAULT_MIN = -20f;
    private static final float DEFAULT_LINE_SPACING = 5f;
    private int graphColor;
    private int numReadings;
    private int lineColor;
    private Paint graphPaint;
    private Paint linePaint;
    private TextPaint textPaint;
    private float[] values;
    private float graphStrokeWidth;
    private float maxValue;
    private float minValue;
    private String units = "";
    private float lineSpacing;
    private RectF graphBounds;
    private float step;
    private int paddingLeft;
    private float zeroY;
    private float valueToPixels;
    private float y;
    private float density;
    private float textCenter;
    private Path path;

    public GraphView(Context context) {
        super(context);
        init(null, 0);
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private static float constrain(float v, float min, float max) {
        return v > max ? max : (v < min ? min : v);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.GraphView, defStyle, 0);

        numReadings = a.getInt(R.styleable.GraphView_numReadings, DEFAULT_NUM_READINGS);
        values = new float[numReadings];
        graphColor = a.getColor(R.styleable.GraphView_graphColor, DEFAULT_GRAPH_COLOR);
        lineColor = a.getColor(R.styleable.GraphView_lineColor, DEFAULT_LINE_COLOR);
        maxValue = a.getFloat(R.styleable.GraphView_max, DEFAULT_MAX);
        minValue = a.getFloat(R.styleable.GraphView_min, DEFAULT_MIN);
        lineSpacing = a.getFloat(R.styleable.GraphView_lineSpacing, DEFAULT_LINE_SPACING);
        units = a.getString(R.styleable.GraphView_units);
        a.recycle();

        density = getResources().getDisplayMetrics().density;

        graphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        graphPaint.setStyle(Paint.Style.STROKE);
        graphPaint.setColor(graphColor);
        graphPaint.setStrokeJoin(Paint.Join.ROUND);
        graphPaint.setStrokeCap(Paint.Cap.BUTT);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2f * density);
        linePaint.setColor(lineColor);
        linePaint.setStrokeCap(Paint.Cap.BUTT);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(lineColor);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(10f * density);

        graphBounds = new RectF();
        path = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final Rect textBounds = new Rect();
        final String s = "000 " + units;
        textPaint.getTextBounds(s, 0, s.length(), textBounds);
        float labelWidth = textBounds.width() + 10f * density;
        textCenter = textBounds.height() / 2f;
        paddingLeft = getPaddingLeft();
        graphBounds.set(labelWidth + paddingLeft, getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
        invalidateMeasurements();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void invalidateMeasurements() {
        step = graphBounds.width() / (float) (numReadings - 1);
        graphStrokeWidth = step * 0.8f;
        graphPaint.setStrokeWidth(graphStrokeWidth);
        graphPaint.setStrokeMiter(graphStrokeWidth / 2f);
        final float valRange = maxValue - minValue;
        valueToPixels = graphBounds.height() / valRange;
        if (minValue >= 0f) {
            zeroY = graphBounds.bottom;
        } else if (maxValue <= 0f) {
            zeroY = graphBounds.top;
        } else {
            zeroY = graphBounds.top + maxValue / valRange * graphBounds.height();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (float v = 0; v <= maxValue; v += lineSpacing) {
            drawLabelsAndLines(canvas, v);
        }
        for (float v = -lineSpacing; v >= minValue; v -= lineSpacing) {
            drawLabelsAndLines(canvas, v);
        }

        path.reset();
        for (int i = 0; i < values.length; i++) {
            if (i == 0) {
                path.moveTo(graphBounds.left + step * i, zeroY - valueToPixels * constrain(values[i], minValue, maxValue));
            }
            path.lineTo(graphBounds.left + step * i, zeroY - valueToPixels * constrain(values[i], minValue, maxValue));
        }
        canvas.drawPath(path, graphPaint);
    }

    private void drawLabelsAndLines(Canvas canvas, float v) {
        y = zeroY - v * valueToPixels;
        canvas.drawText(v + " " + units, paddingLeft, y + textCenter, textPaint);
        canvas.drawLine(graphBounds.left, y, graphBounds.right, y, linePaint);
    }

    public int getGraphColor() {
        return graphColor;
    }

    public void setGraphColor(int graphColor) {
        this.graphColor = graphColor;
        invalidate();
    }

    public int getNumReadings() {
        return numReadings;
    }

    public void setNumReadings(int numReadings) {
        this.numReadings = numReadings;
        values = new float[numReadings];
        invalidateMeasurements();
        invalidate();
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
        invalidate();
    }

    public void updateValue(float value) {
        for (int i = 0; i < values.length - 1; i++) {
            values[i] = values[i + 1];
        }
        values[values.length - 1] = value;
        invalidateMeasurements();
        invalidate();
    }
}
