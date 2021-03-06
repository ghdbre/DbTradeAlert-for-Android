package de.dbremes.dbtradealert;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import de.dbremes.dbtradealert.DbAccess.DbHelper;

public class ReportChartView extends View {
    // region private fields
    private static final String CLASS_NAME = "ReportChartView";
    private static final String naMarker = "-";
    private DbHelper.Extremes quoteExtremes;
    private DbHelper.Extremes targetExtremes;
    // region quote data
    private Float ask;
    private Float basePrice;
    private Float bid;
    private Float daysHigh;
    private Float daysLow;
    private Float lastPrice;
    private Float lowerTarget;
    private Float maxPrice;
    private Float open;
    private Float previousClose;
    private Float trailingTarget;
    private Float upperTarget;
    // endregion quote data
    // region graphics objects
    private final int spreadMarkerHeight = 8;
    private final int paddingY = 4;
    // avoid allocation of object during onDraw():
    private Rect boundsRectTemp = new Rect();
    private Paint linePaint = null;
    private Paint lossPaint = null;
    private Paint textLossPaint = null;
    private Paint textPaint = null;
    private Paint textWinPaint = null;
    private Paint winPaint = null;
    // endregion graphics objects
    // endregion private fields

    // region ctors
    // Constructor required for in-code creation
    public ReportChartView(Context context) {
        super(context);
        init();
    }

    // Constructor required for inflation from resource file
    public ReportChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // Constructor called by subclasses
    public ReportChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    // endregion ctors

    private int drawPrice(Canvas canvas, DbHelper.Extremes extremes,
                          int currentY, float lastPrice, String marker, Float price, int width) {
        // boundsRectTemp.top is < 0 because measured from font's base line
        int originalCurrentY = currentY;
        if (price.isNaN() == false) {
            // Draw prices or their markers centered on their percentage of lastPrice
            float percent = getPercent(lastPrice, price);
            float currentX = getXPositionFromPercentage(extremes, percent, width);
            // Draw tick for lastPrice above chart line
            this.textPaint.getTextBounds("I", 0, 1, this.boundsRectTemp);
            if (price == lastPrice) {
                // textPaint.descent() needed to make tick meet chart line as "I" has no descent
                // boundsRectTemp.bottom == 0
                canvas.drawLine(currentX, currentY - this.boundsRectTemp.top + textPaint.descent(),
                        currentX, currentY, this.textPaint);
            }
            currentY += -this.boundsRectTemp.top + 2 * this.paddingY;
            // Draw marker below chart line
            if (TextUtils.isEmpty(marker) == false) {
                float markerWidth = this.textPaint.measureText(marker);
                float markerXPosition = currentX - markerWidth / 2;
                markerXPosition = ensureTextIsNotCutOff(markerXPosition, markerWidth, width);
                canvas.drawText(marker, markerXPosition,
                        currentY - this.boundsRectTemp.top + this.paddingY, this.textPaint);
            }
            currentY += -this.boundsRectTemp.top + 2 * this.paddingY;
        }
        // Return height of output
        return currentY - originalCurrentY;
    } // drawPrice()

    private float ensureTextIsNotCutOff(float textXPosition, float textWidth, int width) {
        float result = textXPosition;
        if (result - textWidth < 0) {
            result = 0;
        } else if (result > width - textWidth) {
            result = width - textWidth;
        }
        return result;
    } // ensureTextIsNotCutOff()

    private int getMeasureHeight(int heightMeasureSpec) {
        int resultingHeight;
        int desiredHeight = 150;
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

    private float getPercent(float lastPrice, Float price) {
        float result = price * 100 / lastPrice;
        return result;
    } // getPercent()

    private float getXPositionFromPercentage(DbHelper.Extremes extremes, float percent, int width) {
        float result = (percent - extremes.getMinPercent()) * width
                / (extremes.getMaxPercent() - extremes.getMinPercent());
        return result;
    } // getXPositionFromPercentage()

    private void init() {
        // Standard colors from https://material.io/guidelines/style/color.html#color-color-palette
        int standardGreen = Color.parseColor("#4CAF50");
        int standardRed = Color.parseColor("#F44336");
        this.linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.linePaint.setColor(Color.BLACK);
        this.linePaint.setStrokeWidth(2);
        this.lossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.lossPaint.setColor(standardRed);
        this.lossPaint.setStrokeWidth(2);
        this.textLossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textLossPaint.setColor(standardRed);
        this.textLossPaint.setTextSize(30);
        this.textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textPaint.setColor(Color.BLACK);
        this.textPaint.setStrokeWidth(3f);
        this.textPaint.setTextSize(30);
        this.textWinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textWinPaint.setColor(standardGreen);
        this.textWinPaint.setTextSize(30);
        this.winPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.winPaint.setColor(standardGreen);
        this.winPaint.setStrokeWidth(2);
    } // init()

    private void markArea(Canvas canvas, DbHelper.Extremes extremes,
                          int lineY, Float price1, Float price2,
                          float lastPrice, Paint paint, int width) {
        float percent1 = getPercent(lastPrice, price1);
        float position1 = this.getXPositionFromPercentage(extremes, percent1, width);
        float percent2 = getPercent(lastPrice, price2);
        float position2 = this.getXPositionFromPercentage(extremes, percent2, width);
        // drawRect() draws nothing if left > right
        float left = Math.min(position1, position2);
        float right = Math.max(position1, position2);
        canvas.drawRect(left, lineY - this.spreadMarkerHeight / 2,
                right, lineY + this.spreadMarkerHeight / 2, paint);
    } // markArea()

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode()) {
            return;
        }

        int width = getWidth();
        int currentY = this.paddingY;
        // Upper chart shows quote data:
        // - for lastPrice a tick is printed above the chart line
        // - for all other prices a marker is printed below the chart line
        // - spread marked with black rectangle on chart line
        drawPrice(canvas, this.quoteExtremes, currentY, this.lastPrice, "a", this.ask, width);
        drawPrice(canvas, this.quoteExtremes, currentY, this.lastPrice, "b", this.bid, width);
        drawPrice(canvas, this.quoteExtremes, currentY, this.lastPrice, "H", this.daysHigh, width);
        drawPrice(canvas, this.quoteExtremes, currentY, this.lastPrice, "L", this.daysLow, width);
        int outputHeight = drawPrice(
                canvas, this.quoteExtremes, currentY, this.lastPrice, "", this.lastPrice, width);
        drawPrice(canvas, this.quoteExtremes, currentY, this.lastPrice, "O", this.open, width);
        drawPrice(canvas,
                this.quoteExtremes, currentY, this.lastPrice, "P", this.previousClose, width);
        int lineY = outputHeight / 2;
        canvas.drawLine(0, lineY, width, lineY, this.linePaint);
        if (this.ask.isNaN() == false && this.bid.isNaN() == false) {
            markArea(canvas, this.quoteExtremes, lineY, this.ask, this.bid,
                    this.lastPrice, this.linePaint, width);
        }
        currentY += outputHeight + this.paddingY;
        // Lower chart shows target data:
        // - Prices shown like in upper chart
        // - difference between basePrice and lastPrice is marked with green / red rectangle
        drawPrice(canvas, this.targetExtremes, currentY,
                this.lastPrice, "B", this.basePrice, width);
        outputHeight = drawPrice(canvas, this.targetExtremes, currentY,
                this.lastPrice, "", this.lastPrice, width);
        drawPrice(canvas, this.targetExtremes, currentY,
                this.lastPrice, "L", this.lowerTarget, width);
        drawPrice(canvas, this.targetExtremes, currentY,
                this.lastPrice, "T", this.trailingTarget, width);
        drawPrice(canvas, this.targetExtremes, currentY,
                this.lastPrice, "U", this.upperTarget, width);
        lineY = currentY + outputHeight / 2;
        canvas.drawLine(0, lineY, width, lineY, this.linePaint);
        if (this.basePrice.isNaN() == false) {
            // Show performance
            Float performance = (this.lastPrice - this.basePrice) * 100 / this.basePrice;
            Paint performancePaint;
            Paint performanceTextPaint;
            if (performance > 0) {
                performancePaint = this.winPaint;
                performanceTextPaint = this.textWinPaint;
            } else {
                performancePaint = this.lossPaint;
                performanceTextPaint = this.textLossPaint;
            }
            markArea(canvas, this.targetExtremes, lineY,
                    this.basePrice, this.lastPrice, this.lastPrice, performancePaint, width);
            String performanceString = String.format("%01.2f%%", performance);
            performanceTextPaint.getTextBounds(performanceString, 0,
                    performanceString.length(), this.boundsRectTemp);
            int centerX = (width - this.boundsRectTemp.width()) / 2;
            canvas.drawText(performanceString, centerX, lineY - 8, performanceTextPaint);
        }
    } // onDraw()

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredHeight = getMeasureHeight(heightMeasureSpec);
        int measuredWidth = getMeasureWidth(widthMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    } // onMeasure()

    public void setValues(DbHelper.Extremes quoteExtremes, DbHelper.Extremes targetExtremes,
                          Float ask, Float basePrice, Float bid,
                          Float daysHigh, Float daysLow, Float lastPrice, Float lowerTarget,
                          Float maxPrice, Float open, Float previousClose,
                          Float trailingTarget, Float upperTarget) {
        this.quoteExtremes = quoteExtremes;
        this.targetExtremes = targetExtremes;
        this.ask = ask;
        this.basePrice = basePrice;
        this.bid = bid;
        this.daysHigh = daysHigh;
        this.daysLow = daysLow;
        this.lastPrice = lastPrice;
        this.lowerTarget = lowerTarget;
        this.maxPrice = maxPrice;
        this.open = open;
        this.previousClose = previousClose;
        this.trailingTarget = trailingTarget;
        this.upperTarget = upperTarget;
    } // setValues()
}
