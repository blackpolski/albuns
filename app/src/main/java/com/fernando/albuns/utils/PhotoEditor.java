package com.fernando.albuns.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.io.InputStream;
/**
 * Created by 01796085090 on 07/12/2017.
 */

public class PhotoEditor extends View {
    private static final float TOUCH_TOLERANCE = 4;
    private static int SQUARE_SIDE;
    private Path squarePath;
    private Paint squarePaint;
    private float mX;
    private float mY;
    private Bitmap bitmap;
    private Bitmap opaco;
    private Bitmap square;
    private Context context;
    private DisplayMetrics dm;
    private int photoW;
    private int photoH;
    private boolean toDraw = true;
    private Uri imageUri;

    public PhotoEditor(Context c) {
        super(c);
        context = c;
        squarePaint = new Paint();
        squarePath = new Path();
        squarePaint.setAntiAlias(true);
        squarePaint.setColor(Color.RED);
        squarePaint.setStyle(Paint.Style.STROKE);
        squarePaint.setStrokeJoin(Paint.Join.MITER);
        squarePaint.setStrokeWidth(4f);

        adjustDisplayMetrics();
    }

    private void adjustDisplayMetrics() {
        // Ajusta o tamanho padrão da Área de recorte da imagem
        // com base no tamanho da Tela no Telefone
        dm = new DisplayMetrics();

        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        if (Utilitarios.getRotation(context) == Configuration.ORIENTATION_PORTRAIT) {
            SQUARE_SIDE = dm.heightPixels * 300 / 1920; // (250 / 1920) defaul metric
        } else {
            SQUARE_SIDE = dm.heightPixels * 300 / 1080; // (250 / 1080) defaul metric
        }
        mX = SQUARE_SIDE + 40;
        mY = SQUARE_SIDE + 40;

        Log.v("Square_H", "" + SQUARE_SIDE);
        Log.v("mX", "" + mX);
        Log.v("mY", "" + mY);
    }

    public void setupBitmap(final Uri currImageURI) throws PhotoEditorException {
        if (imageUri == null) {
            imageUri = currImageURI;
        } else if (!imageUri.equals(currImageURI)) {
            imageUri = currImageURI;
        }

        try {
            bitmap = setPic(imageUri);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            setLayoutParams(params);
            opaco = doColorFilter(.5, .5, .5);
            invalidate();
        } catch (IOException ex) {
            throw new PhotoEditorException("Erro ao acessar a Foto");
        } catch (PhotoEditorException ex) {
            throw new PhotoEditorException("A Foto não foi encontrada");
        }
    }

    private Bitmap setPic(Uri currImageURI) throws IOException, PhotoEditorException {
        // Get the dimensions of the View
        int targetW = getWidth();
        int targetH = getHeight();

        if (targetH == 0 || targetW == 0) {
            targetW = dm.widthPixels;
            targetH = dm.heightPixels;
        }

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = false;

        Rect rect = new Rect(-1, -1, -1, -1);

        InputStream is = getContext().getContentResolver().openInputStream(currImageURI);
        Bitmap bitmap = BitmapFactory.decodeStream(is, rect, bmOptions);
        is.close();

        if (bitmap == null)
            throw new PhotoEditorException("Falha ao carregar a Foto");

        photoW = bitmap.getWidth();
        photoH = bitmap.getHeight();

        // ajusta a orientação da imagem
        int angle = Utilitarios.getOrientation(context, currImageURI);
        if (angle != 0) {
            Matrix mat = new Matrix();
            mat.postRotate(angle);

            Bitmap oldBitmap = bitmap;

            bitmap = Bitmap.createBitmap(oldBitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
            photoW = bitmap.getWidth();
            photoH = bitmap.getHeight();

            oldBitmap.recycle();
        }

        // Determine how much to scale down the image
        if (Math.abs(photoW - targetW) < Math.abs(photoH - targetH)) {
            bmOptions.outWidth = targetW;
            bmOptions.outHeight = bmOptions.outWidth * photoH / photoW;
        } else {
            bmOptions.outHeight = targetH;
            bmOptions.outWidth = bmOptions.outHeight * photoW / photoH;
        }
        // Após o ajuste verifica se o outro lado é maior que a
        // resolução da tela para reajustar
        if(bmOptions.outHeight > targetH) {
            bmOptions.outHeight = targetH;
            bmOptions.outWidth = bmOptions.outHeight * photoW / photoH;
        } else if(bmOptions.outWidth > targetW) {
            bmOptions.outWidth = targetW;
            bmOptions.outHeight = bmOptions.outWidth * photoH / photoW;
        }

        Bitmap oldBitmap = bitmap;

        bitmap = Bitmap.createScaledBitmap(oldBitmap, bmOptions.outWidth, bmOptions.outHeight, false);

        oldBitmap.recycle();

        return bitmap;
    }

    private Bitmap doColorFilter(double red, double green, double blue) {
        // image size
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // cria o bitmap de saida
        Bitmap bmOut = Bitmap.createBitmap(width, height, bitmap.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // Determina a Área a ser aplicado o filtro
                // get pixel color
                pixel = bitmap.getPixel(x, y);
                A = Color.alpha(pixel);
                R = (int) (Color.red(pixel) * red);
                G = (int) (Color.green(pixel) * green);
                B = (int) (Color.blue(pixel) * blue);
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    public Bitmap getSelectArea() {
        return square;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        adjustDisplayMetrics();
        toDraw = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap != null) {
            Drawable d = getBackground();
            if (d == null)
                d = new BitmapDrawable(getResources(), opaco);

            if (Build.VERSION.SDK_INT >= 16) {
                setBackground(d);  // only api >= 16
            } else {
                setBackgroundDrawable(d);
            }

            if (toDraw || square == null || (square != null && square.isRecycled())) {
                if (square != null)
                    square.recycle();

                square = Bitmap.createBitmap(bitmap, (int) (mX - SQUARE_SIDE),
                        (int) (mY - SQUARE_SIDE), SQUARE_SIDE * 2, SQUARE_SIDE * 2);
                toDraw = false;
            }
            canvas.drawBitmap(square, mX - SQUARE_SIDE, mY - SQUARE_SIDE, squarePaint);
        }
    }

    private void validatePosition() {
        // Mantem a área de seleção dentro do bitmap
        if (mX - SQUARE_SIDE < 0)
            mX = SQUARE_SIDE;
        if (mY - SQUARE_SIDE < 0)
            mY = SQUARE_SIDE;
        if (mX + SQUARE_SIDE > bitmap.getWidth())
            mX = getWidth() - SQUARE_SIDE;
        if (mY + SQUARE_SIDE > bitmap.getHeight())
            mY = getHeight() - SQUARE_SIDE;
    }

    private void touch_start(float x, float y) {
        mX = x;
        mY = y;
        toDraw = true;
        validatePosition();
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mX = x;
            mY = y;
            toDraw = true;
        }
        validatePosition();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
        }
        return true;
    }
}
