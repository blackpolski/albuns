package com.fernando.albuns.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by 01796085090 on 24/11/2017.
 */

public class Utilitarios {
    /*
    * Rotina para ocultar o teclado virtual no momento em que um
    * EditText tem Foco ativo
     */
    public static void hideKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } else {
            final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) activity
                    .findViewById(android.R.id.content)).getChildAt(0);

            activity.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
            );
        }
    }

    /*
    * Rotina para apresentar o teclado virtual no momento em que um
    * EditText tem Foco ativo permitindo o ajuste da tela
     */
    public static void showKeyboard(Activity activity) {
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) activity
                .findViewById(android.R.id.content)).getChildAt(0);

        activity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE |
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );
    }

    /*
    * Carrega a Foto do arquivo em um Bitmap
    */
    public static Bitmap setPic(int width, int height, File fotoUrl, Context context) throws IOException {
        return setFilePic(width,height,fotoUrl,context);
    }

    public static Bitmap setPic(int width, int height, Uri fotoUri, Context context) throws IOException {
        return setFilePic(width,height,fotoUri,context);
    }

    private static Bitmap setFilePic(int width, int height, Object foto, Context context) throws IOException {
        // Get the dimensions of the View
        int targetW = width;
        int targetH = height;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        Rect rect = new Rect(-1, -1, -1, -1);

        InputStream is;
        if(foto instanceof File) {
            is = context.getContentResolver().openInputStream(Uri.fromFile((File)foto));
        } else if(foto instanceof Uri) {
            is = context.getContentResolver().openInputStream((Uri)foto);
        } else {
            throw new IOException("Invalid URI");
        }

        BitmapFactory.decodeStream(is, rect, bmOptions);
        is.close();

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = 0;
        if (targetH != 0 && targetW != 0)
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        //bmOptions.inPurgeable = true;

        Bitmap bitmap;
        if(foto instanceof File) {
            bitmap = BitmapFactory.decodeFile(foto.toString(), bmOptions);
        } else {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor((Uri) foto, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, rect, bmOptions);
            parcelFileDescriptor.close();
        }

        return bitmap;
    }

    /*
     * Obtem a orientação da Foto
     */
    public static int getOrientation(final Context context, final Uri selectedImageUri) {
        int angle = 0;
        try {
            ExifInterface exif = new ExifInterface(selectedImageUri.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;
                case ExifInterface.ORIENTATION_UNDEFINED:
                    String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
                    Cursor cur = context.getContentResolver().query(selectedImageUri, orientationColumn, null, null, null);
                    angle = -1;
                    if (cur != null && cur.moveToFirst()) {
                        angle = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
                        cur.close();
                    }

                    break;
                default:
                    angle = 0;
                    break;
            }

        } catch (IOException ex) {
//            Log.e("getOrientation", "Erro", ex);
        }
        return angle;
    }

    /*
     * Cria um arquivo temporário para armazenar a Foto
     */
    @SuppressLint("SimpleDateFormat")
    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists())
            storageDir.mkdir();
        try {
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            return image;
        } catch (IOException ex) {
            Log.e(imageFileName, storageDir.getPath(), ex);
            throw new IOException(ex);
        }
    }

    /*
     * Salva a imagem contida no arquivo de Foto na Galeria de Fotos
     */
    public static void galleryAddPic(Activity activity, File fotoUrl) {
        Uri contentUri = Uri.fromFile(fotoUrl);
        if(fotoUrl.exists()) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(contentUri);
            activity.sendBroadcast(mediaScanIntent);
        } else {
            Toast.makeText(activity, "A Foto não foi encontrada!", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * Converte um Base64 byte[] em Bitmap
     */
    public static Bitmap bitmapFromBase64(byte[] dados) {
        byte[] fotoArray = Base64.decode(dados, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(fotoArray, 0, fotoArray.length);
        return bitmap;
    }

    /*
     * Converte um Bitmap em Base64 byte[]
     */
    public static byte[] bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encode(byteArray, Base64.DEFAULT);
    }

    /*
     * Extrai o Bitmap de um ImageView
     */
    public static Bitmap bitmapFromImageView(ImageView view) {
        Drawable drawable = view.getDrawable();
        if (drawable != null) {
            if (drawable instanceof ColorDrawable) {
                return convertToBitmap(drawable, view.getWidth(), view.getHeight());
            } else if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
        }
        return null;
    }

    /*
     * Convert um ColorDrawable em Bitmap
     */
    private static Bitmap convertToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        return mutableBitmap;
    }

    /*
     * Cria um Bitmat com um contorno Circular
     */
    public static Bitmap toCircularBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /*
     * Cria um Bitmat com um contorno Circular
     */
    public static Bitmap circularBitmapAndText(int cor, int width, int height, String txt) {
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setColor(cor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(width / 2, height / 2, width / 2, paint);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(100);
        canvas.drawText(txt, width / 2, height / 2 + 30, paint);
        return output;
    }

    /*
     * Utilitário para ler QRCode
     */
    public static void qrCodeReader(Activity activity, int REQUEST_QRCODE_CAPTURE) {
        try {
                /*
                 * Invoca app para escanear o QR Code esperando o texto como resultado
                 */
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes

            activity.startActivityForResult(intent, REQUEST_QRCODE_CAPTURE);

        } catch (Exception e) {
                /*
                 * Caso a aplicação não esteja instalada no Aparelho
                 * Abre o Google Play para que seja instalado
                 */
            Uri marketUri = Uri.parse("http://play.google.com/store/apps/details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
            marketIntent.setData(marketUri);
            activity.startActivity(marketIntent);
        }
    }

    /*
     * Converte um byte[] em Byte[]
     */
    public static Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        int i = 0;
        for (byte b : bytesPrim) bytes[i++] = b; //Autoboxing
        return bytes;
    }

    /*
     * Converte Byte[] to byte[]
     */
    public static byte[] toPrimitives(Byte[] oBytes) {
        byte[] bytes = new byte[oBytes.length];
        for (int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }
        return bytes;
    }

    /*
     * Obtem a orientaçao do telefone
     */
    public static int getRotation(Context context) {
        final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return Configuration.ORIENTATION_PORTRAIT;
            case Surface.ROTATION_90:
                return Configuration.ORIENTATION_LANDSCAPE;
            case Surface.ROTATION_180:
                return Configuration.ORIENTATION_PORTRAIT;
            default:
                return Configuration.ORIENTATION_LANDSCAPE;
        }
    }

    /*
    * Carrega a Foto de uma URI em um Bitmap
    */
    public static class PhotoFromUri extends AsyncTask<Void, Bitmap, Bitmap> {
        private Uri fotoUrl;

        public PhotoFromUri(Uri fotoUrl) {
            this.fotoUrl = fotoUrl;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = null;

            try {
                InputStream is = new URL(fotoUrl.toString()).openStream();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e("PhotoFromUri", ex.getMessage());
            }

            return bitmap;
        }
    }

    /*
     * Converte URI do tipo content:// para file://
     */
    public static String getPathFromContentUri(Context ctx, Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = ctx.getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) {
            return null;
        }

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

}
