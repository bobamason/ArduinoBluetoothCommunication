package org.masonapps.bluetootharduinocomm.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.masonapps.bluetootharduinocomm.R;

/**
 * TODO: document your custom view class.
 */
public class GraphView extends View {
    private static final int DEFAULT_NUM_READINGS = 100;
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
    private float[] values;
    private float graphStrokeWidth;
    private float max;
    private float min;
    private String units = "";
    private float lineSpacing;

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

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.GraphView, defStyle, 0);

        numReadings = a.getInt(R.styleable.GraphView_numReadings, DEFAULT_NUM_READINGS);
        values = new float[numReadings];
        graphColor = a.getColor(R.styleable.GraphView_graphColor, DEFAULT_GRAPH_COLOR);
        lineColor = a.getColor(R.styleable.GraphView_graphColor, DEFAULT_LINE_COLOR);
        max = a.getFloat(R.styleable.GraphView_max, DEFAULT_MAX);
        min = a.getFloat(R.styleable.GraphView_min, DEFAULT_MIN);
        lineSpacing = a.getFloat(R.styleable.GraphView_lineSpacing, DEFAULT_LINE_SPACING);
        units = a.getString(R.styleable.GraphView_units);

        a.recycle();

        graphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        graphPaint.setStyle(Paint.Style.STROKE);
        graphPaint.setColor(graphColor);
        graphPaint.setStrokeCap(Paint.Cap.BUTT);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2f);
        linePaint.setColor(graphColor);
        linePaint.setStrokeCap(Paint.Cap.BUTT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;
        
    }

    public int getGraphColor() {
        return graphColor;
    }

    public void setGraphColor(int graphColor) {
        this.graphColor = graphColor;
    }

    public int getNumReadings() {
        return numReadings;
    }

    public void setNumReadings(int numReadings) {
        this.numReadings = numReadings;
        values = new float[numReadings];
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }
    
    public void addValue(float value){
        for (int i = 0; i < values.length - 1; i++) {
            values[i] = values[i + 1];
        }
        values[values.length - 1] = value;
        invalidate();
    }
}
