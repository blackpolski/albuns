package com.fernando.albuns.view;

/**
 * Created by 01796085090 on 07/12/2017.
 */

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fernando.albuns.R;
import com.fernando.albuns.utils.PhotoEditorActivity;
import com.fernando.albuns.utils.Utilitarios;

import java.io.File;
import java.io.IOException;





public class UserActivity extends AppCompatActivity implements View.OnClickListener {
    // Contasntes utilizadas na chamada a outras Activities
    private static final int REQUEST_CAMERA_PERMISSION = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_EDIT = 2;
    // Constantes que identificam os campos das preferências compastilhadas
    public static final String NOME_USUARIO = "nome";
    public static final String EMAIL_USUARIO = "email";
    public static final String FOTO_USUARIO = "foto";
    public static final String FOTO_URL = "fotourl";
    // Atributos que representam os componentes do Layout
    private EditText edNome;
    private EditText edEmail;
    private ImageView ivFoto;
    // Atributo para armazenas o endereço do arquivo temporário
    // onde a Foto será armazenada durante a captura da imagem
    // pela câmera do telefone
    private File fotoUrl;
    private boolean novaFoto = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializa a referências aos componentes do Layout
        setContentView(R.layout.activity_user);
        edNome = (EditText)findViewById(R.id.edNome);
        edEmail = (EditText)findViewById(R.id.edEmail);
        ivFoto = (ImageView)findViewById(R.id.ivFoto);

        // Habilita o Action Bar
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setHomeButtonEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
        }

        // Oculta o Teclado
        Utilitarios.hideKeyboard(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Carrega as preferëncias compartilhadas que foram salvas previamente e
        // atribui aos componentes do layout
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        edNome.setText(preferences.getString(NOME_USUARIO, ""));
        edEmail.setText(preferences.getString(EMAIL_USUARIO, ""));

        String fotoString = preferences.getString(FOTO_USUARIO, null);
        if (fotoString != null && !novaFoto) {
            Bitmap bitmap = Utilitarios.bitmapFromBase64(fotoString.getBytes());
            ivFoto.setImageBitmap(bitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.salvar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                break;
            case R.id.acao_salvar:

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString(NOME_USUARIO, edNome.getText().toString());
                editor.putString(EMAIL_USUARIO, edEmail.getText().toString());

                Bitmap bitmap = Utilitarios.bitmapFromImageView(ivFoto);
                if (bitmap != null) {
                    editor.putString(FOTO_USUARIO, new String(Utilitarios.bitmapToBase64(bitmap)));
                } else {
                    editor.putString(FOTO_USUARIO, null);
                }

                editor.apply();

                setResult(RESULT_OK);
                break;
        }

        finish();

        return true;
    }

    @Override
    public void onClick(View v) {
        abrirCamera();
    }

    public void abrirCamera()  {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Verifica a autorização para a utilização da câmera
        if (intent.resolveActivity(getPackageManager()) != null) {
            if ((ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(getBaseContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(getBaseContext(),
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                }, REQUEST_CAMERA_PERMISSION);
            } else {
                // Chama a câmera para obter uma foto
                try {
                    fotoUrl = Utilitarios.createImageFile();
                    int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                    if (currentapiVersion > Build.VERSION_CODES.KITKAT) {
                        String pkgName = getApplicationContext().getPackageName();
                        Uri local = FileProvider.getUriForFile(this, pkgName + ".provider", fotoUrl);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, local);
                    } else {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fotoUrl));
                    }

                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                } catch (IOException ex) {
                    Toast.makeText(this, "Falha ao criar arquivo temporário", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Verefica se foi autorizada a utilização da câmera
        boolean autorizado = true;

        for(int resultato : grantResults) {
            if(resultato == PackageManager.PERMISSION_DENIED) {
                autorizado = false;
                break;
            }
        }

        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if(autorizado)
                    abrirCamera();
                else
                    Toast.makeText(this, "O Acesso à Câmera foi negado!", Toast.LENGTH_LONG).show();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Carrega a foto após a captura da imagem pela câmera
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) { // Recebe a foto da Camera
                // Chama o Editor de Fotos
                Intent edFoto = new Intent(this, PhotoEditorActivity.class);
                edFoto.putExtra("PHOTO_URL", fotoUrl.toString());
                startActivityForResult(edFoto, REQUEST_IMAGE_EDIT);
            } else if(requestCode == REQUEST_IMAGE_EDIT) {
                Bundle dados = data.getExtras();
                if (dados != null) {
                    try {
                        File bitmapUrl = new File(dados.getString("PHOTO_URL"));
                        Bitmap bitmap = Utilitarios.setPic(ivFoto.getWidth(), ivFoto.getHeight(), bitmapUrl, this);
                        ivFoto.setImageBitmap(bitmap);
                        ivFoto.invalidate();
                        novaFoto = true;
                    } catch (IOException ex) {
                        Toast.makeText(this, "Falha ao capturar a Foto", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Salva o conteúdo dos atributos em caso de recarga da activity atual
        outState.putString(NOME_USUARIO, edNome.getText().toString());
        outState.putString(EMAIL_USUARIO, edEmail.getText().toString());
        outState.putString(FOTO_URL, fotoUrl.getAbsolutePath());
        Bitmap bitmap = Utilitarios.bitmapFromImageView(ivFoto);
        if(bitmap != null)
            outState.putByteArray(FOTO_USUARIO, Utilitarios.bitmapToBase64(bitmap));

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restaura o conteúdo dos atributos após a recarga da activity atual
        super.onRestoreInstanceState(savedInstanceState);

        edNome.setText(savedInstanceState.getString(NOME_USUARIO));
        edEmail.setText(savedInstanceState.getString(EMAIL_USUARIO));
        fotoUrl = new File(savedInstanceState.getString(FOTO_URL));
        byte[] bytes = savedInstanceState.getByteArray(FOTO_USUARIO);
        if(bytes != null) {
            Bitmap bitmap = Utilitarios.bitmapFromBase64(bytes);
            ivFoto.setImageBitmap(bitmap);
        }
    }
}