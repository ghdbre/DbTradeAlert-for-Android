package de.dbremes.dbtradealert;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class QuoteChartView extends View {
    private static final String CLASS_NAME = "QuoteChartView";
    private Paint textPaint = null;

    // region ctors
    // Constructor required for in-code creation
    public QuoteChartView(Context context) {
        super(context);
        init();
    }

    // Constructor required for inflation from resource file
    public QuoteChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // Constructor called by subclasses
    public QuoteChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    // endregion ctors

    private int getMeasureHeight(int heightMeasureSpec) {
        int resultingHeight;
        int desiredHeight = 100;
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            // Must be this size
            resultingHeight = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...
            resultingHeight = Math.min(desiredHeight, heightSize);
        } else {
            // Be whatever you want
            resultingHeight = desiredHeight;
        }
        return resultingHeight;
    } // getMeasureHeight()

    private int getMeasureWidth(int widthMeasureSpec) {
        int resultingWidth;
        int desiredWidth = 500;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            // Must be this size
            resultingWidth = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...
            resultingWidth = Math.min(desiredWidth, widthSize);
        } else {
            // Be whatever you want
            resultingWidth = desiredWidth;
        }
        return resultingWidth;
    } // getMeasureWidth()

    private void init() {
        this.textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textPaint.setColor(Color.GREEN);
        this.textPaint.setTextSize(50);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final String text = "GOOG goes up!";
        Rect boundsRect = new Rect();
        this.textPaint.getTextBounds(text, 0, text.length(), boundsRect);
        canvas.drawText(text, 1, -boundsRect.top + 1, this.textPaint);
        Log.v(CLASS_NAME, String.format(
                "%s: canvas.Height = %d; canvas.Width = %d",
                "onDraw", canvas.getHeight(), canvas.getWidth()));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredHeight = getMeasureHeight(heightMeasureSpec);
        int measuredWidth = getMeasureWidth(widthMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    } // onMeasure()
}
