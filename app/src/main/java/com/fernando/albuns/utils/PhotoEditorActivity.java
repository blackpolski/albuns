package com.fernando.albuns.utils;

/**
 * Created by 01796085090 on 07/12/2017.
 */


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoEditorActivity extends Activity {
    private PhotoEditor mv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove o Titulo da Activity
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundColor(Color.BLACK);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));


        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

        mv = new PhotoEditor(this);
        layout.addView(mv, params);

        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

        Button btSalvar = new Button(getBaseContext());
        btSalvar.setText("Salvar");
        btSalvar.setLayoutParams(params);
        layout.addView(btSalvar);
        btSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Área da foto selecionada
                    Bitmap bitmap = mv.getSelectArea();

                    // Salva a imagen editada em arquivo
                    File bitmapFile = Utilitarios.createImageFile();
                    FileOutputStream fo = new FileOutputStream(bitmapFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fo);
                    fo.close();
                    bitmap.recycle();

                    // retorna a URL do arquivo salvo para a Activity Chamadora
                    Intent result = new Intent();
                    result.putExtra("PHOTO_URL", bitmapFile.toString());
                    setResult(RESULT_OK, result);
                } catch (IOException ex) {
                    Log.e("PhotoEditor", "Erro", ex);
                }
                finish();
            }
        });

        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

        Button btCancelar = new Button(getBaseContext());
        btCancelar.setText("Cancelar");
        btCancelar.setLayoutParams(params);
        layout.addView(btCancelar);
        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setContentView(layout);

        // Carrega a imagem do arquivo para o ImageView
        Intent request = getIntent();
        if (request != null) {
            Bundle dados = request.getExtras();
            if (dados != null) {
                String url = dados.getString("PHOTO_URL");
                if (url != null) {
                    try {
                        if (url.startsWith("content:")) {
                            Uri fotoUri = Uri.parse(url);
                            mv.destroyDrawingCache(); // <==
                            mv.setupBitmap(fotoUri);
                        } else {
                            File fotoURL = new File(url);
                            mv.destroyDrawingCache(); // <==
                            mv.setupBitmap(Uri.fromFile(fotoURL));
                        }
                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                } else {
                    finish();
                }
            }
        }
    }

}