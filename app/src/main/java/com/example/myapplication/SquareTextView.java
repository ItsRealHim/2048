package com.example.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * A TextView that forces its height to be the same as its width, creating a square.
 */
public class SquareTextView extends AppCompatTextView {

    public SquareTextView(@NonNull Context context) {
        super(context);
    }

    public SquareTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Set the measured height to be the same as the width.
        // This forces the view to be a square.
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
