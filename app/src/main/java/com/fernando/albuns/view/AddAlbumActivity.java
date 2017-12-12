package com.fernando.albuns.view;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.fernando.albuns.R;
import com.fernando.albuns.model.bean.Album;
import com.fernando.albuns.utils.DateDialog;
import com.fernando.albuns.model.dao.AlbumDao;
import com.fernando.albuns.utils.Utilitarios;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by 01796085090 on 22/11/2017.
 */

public class AddAlbumActivity extends AppCompatActivity {
    // Referência do AlbumDao a ser utilizada na activity
    public static final int REQUEST_GALERY_PERMISSION = 1;
    public static final int REQUEST_IMAGE_GALERY = 0;
    private AlbumDao dao = AlbumDao.manager;
    private EditText edBanda;
    private EditText edAlbum;
    private EditText edGenero;
    private EditText edLancamento;
    private ImageView ivFotoAlbum;
    private Bitmap bitmap;
    // Referência do objeto Album que está em edição no momento
    private Album album;
    private Calendar calendar = Calendar.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_album);

        edBanda        = (EditText)  findViewById(R.id.idBanda);
        edAlbum        = (EditText)  findViewById(R.id.idAlbum);
        edGenero       = (EditText)  findViewById(R.id.idGenero);
        edLancamento   = (EditText)  findViewById(R.id.idLancamento);
        ivFotoAlbum    = (ImageView) findViewById(R.id.iv_capa_album);


        Intent intent = getIntent();
        if(intent != null) {
            Bundle dados = intent.getExtras();
            if(dados != null) {
                long id = dados.getLong("id");
                album = dao.getAlbum(id);
                carregaInfTela();
            }
        }

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        Bitmap bitmap = Utilitarios.bitmapFromImageView(ivFotoAlbum);

        if(bitmap != null){
            outState.putByteArray("fotoAlbum", Utilitarios.bitmapToBase64(bitmap));
        }else{
            outState.putByteArray("fotoAlbum", null);
        }

       if(album != null) // necessário para aceitar a adição da foto sem ter sido criado o id ainda
            outState.putLong("id", album.getId());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        byte[] bytes = savedInstanceState.getByteArray("fotoAlbum");
        if(bytes != null){
            ivFotoAlbum.setImageBitmap(Utilitarios.bitmapFromBase64(bytes));
            ivFotoAlbum.setBackgroundColor(Color.TRANSPARENT);
        }

        Long id = savedInstanceState.getLong("id");
        album = dao.getAlbum(id);
        carregaInfTela();



        super.onRestoreInstanceState(savedInstanceState);
    }

    private void carregaInfTela() {
        if (album != null) {
            edBanda.setText(album.getBanda());
            edAlbum.setText(album.getAlbum());
            edGenero.setText(album.getGenero());
            edLancamento.setText(album.getLancamentoFmt());
            if (album.getBitmap() != null) {
                ivFotoAlbum.setImageBitmap(album.getBitmap());
                ivFotoAlbum.setBackgroundColor(Color.TRANSPARENT);
            }
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
                setResult(Activity.RESULT_CANCELED);
                break;
            case R.id.acao_salvar:
                if(album == null) {
                    album = new Album();
                }

                album.setBanda(edBanda.getText().toString());
                album.setAlbum(edAlbum.getText().toString());
                album.setGenero(edGenero.getText().toString());
                album.setLancamento(calendar.getTime());

                if(bitmap != null) {
                    album.setBitmap(bitmap);
                }

                dao.salvar(album);

                setResult(Activity.RESULT_OK);
                break;
        }

        finish();

        return true;
    }

    public void selecionaData(View v) {
        DateDialog.makeDialog(calendar, edLancamento)
                .show(getFragmentManager(), "Data de Lançamento");

    }

    public void insereFoto(View view){
        abrirGaleria();
    }

    private void abrirGaleria(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        if (intent.resolveActivity(getPackageManager()) != null){
            if ((ContextCompat.checkSelfPermission(getBaseContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)){

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALERY_PERMISSION);
            }else{
                startActivityForResult(Intent.createChooser(intent, "Selecione a foto"),
                REQUEST_IMAGE_GALERY);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean autorizado = true;

        for (int resultado : grantResults){
            if (resultado == PackageManager.PERMISSION_DENIED){
                autorizado = false;
                break;
            }
        }
        switch (requestCode){
            case REQUEST_GALERY_PERMISSION:
                if (autorizado){
                    abrirGaleria();
                }
                else
                    Toast.makeText(this, "Acesso à galeria negado", Toast.LENGTH_SHORT).show();
                break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_GALERY){
            if (data != null){
                try {
                    Uri imageURI = data.getData();
                    Bitmap bitmap = Utilitarios.setPic(ivFotoAlbum.getWidth(), ivFotoAlbum.getHeight(), imageURI, this);
                    ivFotoAlbum.setImageBitmap(bitmap);
                    ivFotoAlbum.invalidate();

                    this.ivFotoAlbum.setBackgroundColor(Color.TRANSPARENT);

                    salvaFoto();

                } catch (IOException e){
                    Toast.makeText(this, "Falha ao abrir a foto", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void salvaFoto(){
        bitmap = Utilitarios.bitmapFromImageView(ivFotoAlbum);
        if (bitmap != null){
            ivFotoAlbum.setImageBitmap(bitmap);
        }
    }

}
