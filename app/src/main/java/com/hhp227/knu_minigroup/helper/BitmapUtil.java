package com.hhp227.knu_minigroup.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import java.io.FileNotFoundException;

public class BitmapUtil {
    private Context context;

    public BitmapUtil(Context context) {
        this.context = context;
    }

    public Bitmap bitmapResize(Uri uri, int resize) {
        Bitmap result = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);

            int width = options.outWidth;
            int height = options.outHeight;
            int sampleSize = 1;

            while (true) {
                if (width / 2 < resize || height / 2 < resize)
                    break;
                width /= 2;
                height /= 2;
                sampleSize *= 2;
            }
            options.inSampleSize = sampleSize;
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
            result = bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Bitmap rotateImage(Bitmap bitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
