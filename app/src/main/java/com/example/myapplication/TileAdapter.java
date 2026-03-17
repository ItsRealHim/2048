package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class TileAdapter extends BaseAdapter {

    private final Context context;
    private final int[][] board;

    public TileAdapter(Context context, int[][] board) {
        this.context = context;
        this.board = board;
    }

    @Override
    public int getCount() {
        return 16; // 4x4 grid
    }

    @Override
    public Object getItem(int position) {
        int row = position / 4;
        int col = position % 4;
        return board[row][col];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int tileValue = (int) getItem(position);

        // Use our new SquareTextView
        SquareTextView textView;

        if (convertView == null) {
            // If the view is new, create our custom SquareTextView and configure it
            textView = new SquareTextView(context);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(24f); // Use sp for scalable text in production apps
        } else {
            // Otherwise, reuse the old view, which is already a SquareTextView
            textView = (SquareTextView) convertView;
        }

        // Set text and colors based on the current tile value
        if (tileValue > 0) {
            textView.setText(String.valueOf(tileValue));
            textView.setBackgroundColor(getTileColor(tileValue));
            // Tiles > 4 get white text
            textView.setTextColor(tileValue > 4 ? Color.WHITE : Color.parseColor("#776E65"));
        } else {
            textView.setText("");
            // Color for empty cells
            textView.setBackgroundColor(Color.parseColor("#CDC1B4"));
        }

        return textView;
    }

    // Maps tile values to their specific background colors (this method is unchanged)
    private int getTileColor(int value) {
        switch (value) {
            case 2:
                return Color.parseColor("#EEE4DA");
            case 4:
                return Color.parseColor("#EDE0C8");
            case 8:
                return Color.parseColor("#F2B179");
            case 16:
                return Color.parseColor("#F59563");
            case 32:
                return Color.parseColor("#F67C5F");
            case 64:
                return Color.parseColor("#F65E3B");
            case 128:
                return Color.parseColor("#EDCF72");
            case 256:
                return Color.parseColor("#EDCC61");
            case 512:
                return Color.parseColor("#EDC850");
            case 1024:
                return Color.parseColor("#EDC53F");
            case 2048:
                return Color.parseColor("#EDC22E");
            default:
                return Color.LTGRAY; // For values > 2048
        }
    }
}
