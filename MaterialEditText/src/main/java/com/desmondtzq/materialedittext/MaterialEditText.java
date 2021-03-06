package com.desmondtzq.materialedittext;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.EditText;


public class MaterialEditText extends EditText {
    private static final int ROBOTO = 0;
    private static final int ROBOTO_THIN = 1;
    private static final int ROBOTO_LIGHT = 2;
    private static final int ROBOTO_MEDIUM = 3;
    private static final int ROBOTO_BOLD = 4;
    private static final int ROBOTO_BLACK = 5;

    private static final int ANIMATION_NONE = 0;
    private static final int ANIMATION_SHOW = 1;
    private static final int ANIMATION_HIDE = 2;

    private final Paint labelPaint = new Paint();
    private ColorStateList labelColor;
    private float labelScale;

    private int animationSteps;
    private int animationType = ANIMATION_NONE;
    private int animationFrame;

    private boolean isEmpty;
    private boolean isFocused;

    public MaterialEditText(Context context) {
        super(context);
    }

    public MaterialEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.drawable.material_edittext_bg);

        parseAttributes(context, attrs);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.label_scale, typedValue, true);
        labelScale = typedValue.getFloat();
        animationSteps = getResources().getInteger(R.integer.label_animation_step);

        labelColor = getHintTextColors();
        isEmpty = TextUtils.isEmpty(getText());
    }

    public MaterialEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setBackgroundResource(R.drawable.material_edittext_bg);

        parseAttributes(context, attrs);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.label_scale, typedValue, true);
        labelScale = typedValue.getFloat();
        animationSteps = getResources().getInteger(R.integer.label_animation_step);

        labelColor = getHintTextColors();
        isEmpty = TextUtils.isEmpty(getText());
    }

    public void parseAttributes(Context context, AttributeSet attrs) {
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.MaterialEditText);
        int typeface = values.getInt(R.styleable.MaterialEditText_typeface, 0);

        switch (typeface) {
            case ROBOTO: default:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf"));
                break;

            case ROBOTO_BLACK:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "Roboto-Black.ttf"));
                break;

            case ROBOTO_BOLD:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "Roboto-Bold.ttf"));
                break;

            case ROBOTO_LIGHT:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf"));
                break;

            case ROBOTO_MEDIUM:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf"));
                break;

            case ROBOTO_THIN:
                setTypeface(Typeface.createFromAsset(context.getAssets(), "Roboto-Thin.ttf"));
                break;
        }

        values.recycle();
    }

    @Override
    public int getCompoundPaddingTop() {
        final Paint.FontMetricsInt metrics = getPaint().getFontMetricsInt();
        final int floatingHintHeight = (int) ((metrics.bottom - metrics.top) * labelScale);
        return super.getCompoundPaddingTop() + floatingHintHeight;
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        final boolean isEmptyNow = TextUtils.isEmpty(getText());

        if(isEmpty == isEmptyNow) {
            return;
        }

        isEmpty = isEmptyNow;

        if(!isShown()) {
            return;
        }

        if(isEmpty) {
            animationType = ANIMATION_HIDE;
            setHintTextColor(Color.TRANSPARENT);
        } else {
            animationType = ANIMATION_SHOW;
        }
    }

    @Override
    public void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

        isFocused = gainFocus;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // if the EditText do not have hint, do nothing
        if(TextUtils.isEmpty(getHint())) {
            return;
        }

        final boolean isAnimating = animationType != ANIMATION_NONE;

        // if EditText is empty, do nothing as hint will be drawn by Android
        if(!isAnimating && TextUtils.isEmpty(getText())) {
            return;
        }

        labelPaint.set(getPaint());
        labelPaint.setColor(isFocused ? getResources().getColor(R.color.label_color) : Color.GRAY);

        final float hintPosX = getCompoundPaddingLeft() + getScrollX();
        final float hintPosY = getBaseline();
        final float floatLabelPosY = hintPosY + getPaint().getFontMetricsInt().top - 8;
        final float hintSize = getTextSize();
        final float floatLabelSize = hintSize * labelScale;

        if(!isAnimating) {
            labelPaint.setTextSize(floatLabelSize);
            canvas.drawText(getHint().toString(), hintPosX, floatLabelPosY, labelPaint);
            return;
        }

        if(animationType == ANIMATION_SHOW) {
            startAnimation(canvas, hintSize, floatLabelSize, hintPosX, hintPosY, floatLabelPosY);
        } else {
            startAnimation(canvas, floatLabelSize, hintSize, hintPosX, floatLabelPosY, hintPosY);
        }

        animationFrame++;

        if(animationFrame == animationSteps) {
            if(animationType == ANIMATION_HIDE) {
                setHintTextColor(labelColor);
            }
            animationType = ANIMATION_NONE;
            animationFrame = 0;
        }

        invalidate();
    }

    private void startAnimation(Canvas canvas, float fromSize, float toSize, float hintPosX,
                                float fromY, float toY) {
        final float textSize = calculate(fromSize, toSize);
        final float hintPosY = calculate(fromY, toY);
        labelPaint.setTextSize(textSize);
        canvas.drawText(getHint().toString(), hintPosX, hintPosY, labelPaint);
    }

    private float calculate(float from, float to) {
        final float alpha = (float) animationFrame / (animationSteps - 1);
        return (from * (1 - alpha)) + (to * alpha);
    }
}
