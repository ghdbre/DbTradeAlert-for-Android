package de.dbremes.dbtradealert;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import de.dbremes.dbtradealert.DbAccess.DbHelper;

public class QuoteChartView extends View {
    // region private fields
    private static final String CLASS_NAME = "QuoteChartView";
    private static final String naMarker = "-";
    private DbHelper.ExtremesInfo extremesInfo;
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
    private Float upperTarget;
    // region graphics objects
    private final int spreadMarkerHeight = 8;
    private final int yPadding = 4;
    // avoid allocation of object during onDraw():
    private Rect boundsRectTemp = new Rect();
    private Paint linePaint = null;
    private Paint textPaint = null;
    // endregion graphics objects
    // endregion private fields

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

    private int drawPrice(Canvas canvas,
                          int currentY, float lastPrice, String marker, Float price, int width) {
        int result = 0;
        String valueString = "";
        if (price != Float.NaN) {
            float percent = getPercent(lastPrice, price);
            float currentX = getXPositionFromPercentage(percent, width);
            // Draw lastPrice centered above chart line
            valueString = String.format("%01.2f", price);
            this.textPaint.getTextBounds(
                    valueString, 0, valueString.length(), this.boundsRectTemp);
            if (price == lastPrice) {
                canvas.drawText(valueString,
                        currentX - this.boundsRectTemp.width() / 2,
                        currentY - this.boundsRectTemp.top, this.textPaint);
            }
            result += -this.boundsRectTemp.top + 2 * this.yPadding;
            // Draw marker centered below chart line
            if (marker.isEmpty() == false) {
                float markerWidth = this.textPaint.measureText(marker);
                float makerXPosition = currentX - markerWidth / 2;
                // Avoid marker getting cut off
                if (makerXPosition < 0) {
                    makerXPosition = 0;
                } else if (makerXPosition > width - markerWidth) {
                    makerXPosition = width - markerWidth;
                }
                canvas.drawText(marker, makerXPosition,
                        result - this.boundsRectTemp.top + this.yPadding, this.textPaint);
            }
            result += -this.boundsRectTemp.top + 2 * this.yPadding;
        }
        return result;
    } // drawPrice()

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

    private float getPercent(float lastPrice, Float price) {
        float result = price * 100 / lastPrice;
        return result;
    } // getPercent()

    private float getXPositionFromPercentage(float percent, int width) {
        float result =
                (percent - this.extremesInfo.getMinPercent()) * width
                        / (this.extremesInfo.getMaxPercent() - this.extremesInfo.getMinPercent());
        return result;
    } // getXPositionFromPercentage()

    private void init() {
        this.linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.linePaint.setColor(Color.BLACK);
        this.linePaint.setStrokeWidth(2);
        this.textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textPaint.setColor(Color.BLACK);
        this.textPaint.setTextSize(30);
    } // init()

    private void markSpread(Canvas canvas,
                            int lineY, Float ask, Float bid, float lastPrice, int width) {
        float askPercent = getPercent(lastPrice, ask);
        float askPosition = this.getXPositionFromPercentage(askPercent, width);
        float bidPercent = getPercent(lastPrice, bid);
        float bidPosition = this.getXPositionFromPercentage(bidPercent, width);
        canvas.drawRect(bidPosition, lineY - this.spreadMarkerHeight / 2,
                askPosition, lineY + this.spreadMarkerHeight / 2, this.linePaint);
    } // markSpread()

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int currentY = this.yPadding;
        int outputHeight;
        // 1st chart shows quote data
        drawPrice(canvas, currentY, this.lastPrice, "a", this.ask, width);
        drawPrice(canvas, currentY, this.lastPrice, "b", this.bid, width);
        drawPrice(canvas, currentY, this.lastPrice, "H", this.daysHigh, width);
        drawPrice(canvas, currentY, this.lastPrice, "L", this.daysLow, width);
        drawPrice(canvas, currentY, this.lastPrice, "", this.lastPrice, width);
        drawPrice(canvas, currentY, this.lastPrice, "O", this.open, width);
        outputHeight = drawPrice(canvas, currentY, this.lastPrice, "P", this.previousClose, width);
        // Draw chart line
        int lineY = outputHeight / 2;
        canvas.drawLine(0, lineY, width, lineY, this.linePaint);
        if (ask != Float.NaN && bid != Float.NaN) {
            markSpread(canvas, lineY, ask, bid, lastPrice, width);
        }
        currentY += outputHeight + 2;
    } // onDraw()

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredHeight = getMeasureHeight(heightMeasureSpec);
        int measuredWidth = getMeasureWidth(widthMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    } // onMeasure()

    public void setValues(DbHelper.ExtremesInfo extremesInfo, Float ask, Float basePrice, Float bid,
                          Float daysHigh, Float daysLow, Float lastPrice, Float lowerTarget,
                          Float maxPrice, Float open, Float previousClose, Float upperTarget) {
        this.extremesInfo = extremesInfo;
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
        this.upperTarget = upperTarget;
    } // setValues()
}
