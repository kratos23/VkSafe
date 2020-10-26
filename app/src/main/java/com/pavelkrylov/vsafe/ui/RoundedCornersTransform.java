package com.pavelkrylov.vsafe.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;


public class RoundedCornersTransform implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {

        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        float r = Resources.getSystem().getDisplayMetrics().density * 8;
        canvas.drawRoundRect(new RectF(0, 0, width, height), r, r, paint);
        source.recycle();

        return bitmap;
    }

    @Override
    public String key() {
        return "rounded_corners";
    }
}
