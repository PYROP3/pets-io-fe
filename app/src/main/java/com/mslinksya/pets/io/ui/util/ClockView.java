package com.mslinksya.pets.io.ui.util;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.mslinksya.pets.io.R;
import com.mslinksya.pets.io.utils.Log;

import java.util.Calendar;

public class ClockView extends View {

    private static final String TAG = ClockView.class.getSimpleName();

    private int height, width = 0;
    private int padding = 0;
    private int fontSize = 0;
    private int handTruncation, hourHandTruncation = 0;
    private int radius = 0;
    private Paint paint;
    private boolean isInit;
    private final int[] numbers = {1,2,3,4,5,6,7,8,9,10,11,12};
    private final Rect rect = new Rect();
    private Calendar calendar;

    public ClockView(Context context) {
        super(context);
        this.calendar = Calendar.getInstance();
    }

    public ClockView(Context context, Calendar calendar) {
        super(context);
        this.calendar = calendar;
        Log.d(TAG, "new ClockView : " + calendar.toString());
    }

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.calendar = Calendar.getInstance();
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.calendar = Calendar.getInstance();
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private void initClock() {
        height = getHeight();
        width = getWidth();
        int numeralSpacing = 0;
        padding = numeralSpacing + 35;
        fontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13,
                getResources().getDisplayMetrics());
        int min = Math.min(height, width);
        radius = min / 2 - padding;
        handTruncation = min / 20;
        hourHandTruncation = min / 7;
        paint = new Paint();
        isInit = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isInit) {
            initClock();
        }
        Log.d(TAG, "onDraw");

        drawCircle(canvas);
        drawNumeral(canvas);
        drawHands(canvas);
        drawCenter(canvas);
    }

    private void drawHand(Canvas canvas, double loc, boolean isHour, boolean isSecond) {
        double angle = Math.PI * loc / 30 - Math.PI / 2;
        int handRadius = isHour ? radius - handTruncation - hourHandTruncation : radius - handTruncation;
        if (isSecond) paint.setColor(getResources().getColor(android.R.color.holo_red_light));
        canvas.drawLine((int)(width / 2), (int)(height / 2),
                (float) (width / 2 + Math.cos(angle) * handRadius),
                (float) (height / 2 + Math.sin(angle) * handRadius),
                paint);
        if (isSecond) paint.setColor(getStrokeColor());
    }

    private void drawHands(Canvas canvas) {
        float hour = calendar.get(Calendar.HOUR_OF_DAY);
        hour = hour > 12 ? hour - 12 : hour;
        drawHand(canvas, (hour + calendar.get(Calendar.MINUTE) / 60.) * 5f, true, false);
        drawHand(canvas, calendar.get(Calendar.MINUTE), false, false);
        drawHand(canvas, calendar.get(Calendar.SECOND), false, true);
    }

    private void drawNumeral(Canvas canvas) {
        paint.setTextSize(fontSize);

        for (int number : numbers) {
            String tmp = String.valueOf(number);
            paint.getTextBounds(tmp, 0, tmp.length(), rect);
            double angle = Math.PI / 6 * (number - 3);
            int x = (int) (width / 2 + Math.cos(angle) * radius - rect.width() / 2);
            int y = (int) (height / 2 + Math.sin(angle) * radius + rect.height() / 2);
            canvas.drawText(tmp, x, y, paint);
        }
    }

    private void drawCenter(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle((int)(width / 2), (int)(height / 2), 12, paint);
    }

    private void drawCircle(Canvas canvas) {
        paint.reset();
        paint.setColor(getFillColor());
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle((int)(width / 2), (int)(height / 2), radius + padding - 10, paint);
        paint.reset();
        paint.setColor(getStrokeColor());
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        canvas.drawCircle((int)(width / 2), (int)(height / 2), radius + padding - 10, paint);
    }

    private int getStrokeColor() {
        return isDaytime()
                ? getResources().getColor(R.color.clock_stroke_daytime)
                : getResources().getColor(R.color.clock_stroke_nighttime);
    }

    private int getFillColor() {
        return isDaytime()
                ? getResources().getColor(R.color.clock_fill_daytime)
                : getResources().getColor(R.color.clock_fill_nighttime);
    }

    private boolean isDaytime() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour >= 6 && hour <= 18);
    }
}
