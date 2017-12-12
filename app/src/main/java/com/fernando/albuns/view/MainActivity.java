package com.fernando.albuns.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.fernando.albuns.R;
import com.fernando.albuns.model.dao.AlbumDao;
import com.fernando.albuns.utils.Utilitarios;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener,
        DialogInterface.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener{
    private AlbumDao dao = AlbumDao.manager;
    private ListView listView;
    private MenuItem itEditar;
    private MenuItem itApagar;

    private TextView tvNome;
    private TextView tvEmail;
    private ImageView ivFoto;

    private AlbumAdapter itemLista;
    // Identificação para a chamada a Activity EditarCadastrar
    private final int EDITA_ALBUM = 0;
    private final int NOVO_ALBUM = 1;
    private final int NOVA_ORDEM = 2;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        itemLista = new AlbumAdapter(this);

        //Configuração do Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.btAdiciona);
        fab.setOnClickListener(this);

        //Configuração do menu de navegação
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Registro dos menus para tratar ações do menu de navegação
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Inicializa os atributos do Navigation Drawer
        View cabecalho = navigationView.getHeaderView(0);
        tvNome = cabecalho.findViewById(R.id.tvNome);
        tvEmail = cabecalho.findViewById(R.id.tvEmail);
        ivFoto = cabecalho.findViewById(R.id.ivFoto);

        // Cria o Adapter para que ele forneça os dados para o ListView
        itemLista = new AlbumAdapter(this);

        listView = findViewById(R.id.listaAlbum);
        // Registra o adapter no ListView
        listView.setAdapter(itemLista);
        // Solicita ao listView que qualquer click em um item do listView
        // será redirecionado ao médoto "onItemClick" desta classe (MainActivity)
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        tvNome.setText(preferences.getString(UserActivity.NOME_USUARIO, ""));
        tvEmail.setText(preferences.getString(UserActivity.EMAIL_USUARIO, ""));

        String fotoString = preferences.getString(UserActivity.FOTO_USUARIO, null);
        if (fotoString != null){
            Bitmap bitmap = Utilitarios.bitmapFromBase64(fotoString.getBytes());
            ivFoto.setImageBitmap(Utilitarios.toCircularBitmap(bitmap));
        }

    }

    @Override
    public void onClick(View view) {
        // é criado um Intent para definir ao Android qual Activity será chamada
        Intent tela = new Intent(getBaseContext(), AddAlbumActivity.class);
        // é solicitado ao Android que seja iniciada a execução de uma nova Activity
        // porém também é solicitado informar quando esta activity retornar
        startActivityForResult(tela, NOVO_ALBUM);
    }

    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id){
            case R.id.nav_preferencias:
                Intent intent = new Intent(this, PreferenciasActivity.class);
                startActivityForResult(intent, NOVA_ORDEM);

                break;
            case R.id.nav_perfil:
                Intent perfil = new Intent(this, UserActivity.class);
                startActivity(perfil);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Registra o menu das opções de exclusão de itens da lista
        getMenuInflater().inflate(R.menu.apagar_menu, menu);
        // Obtem a referencia dos itens do menu
        itEditar = (MenuItem)menu.findItem(R.id.acao_editar);
        itApagar = (MenuItem)menu.findItem(R.id.acao_apagar);
        // oculta o item de menu que apaga os itens
        itApagar.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.acao_editar:
                itemLista.trocouOLayout(TipoDeDetalhe.EXCLUSAO);
                itEditar.setVisible(false);
                itApagar.setVisible(true);
                break;
            case R.id.acao_apagar:
                if(dao.temAlbumPraApagar()) {
                    // Apresenta um alerta de confirmação
                    AlertDialog.Builder alerta = new AlertDialog.Builder(this);
                    alerta.setMessage("Confirma a exclusão dos Álbuns?");
                    alerta.setNegativeButton("Não", null);
                    alerta.setPositiveButton("Sim", this);
                    alerta.create();
                    alerta.show();
                } else {
                    mudaLayout();
                }
                break;
        }

        return true;
    }

    private void mudaLayout() {
        itemLista.trocouOLayout(TipoDeDetalhe.EDICAO);
        itEditar.setVisible(true);
        itApagar.setVisible(false);
    }

    // Executa a exclusão dos Albuns quando selecionado a
    // opção "Sim" no Alerta de confirmação
    @Override
    public void onClick(DialogInterface dialog, int botao) {
        dao.apagarOsSelecionados();
        mudaLayout();
    }

    /*
           Este método trata da ação do click nos itens da lista
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int linha, long id) {
        // é criado um Intent para definir ao Android qual Activity será chamada
        Intent tela = new Intent(getBaseContext(), AddAlbumActivity.class);
        // é passado o ID do objeto para a nova Activity a fim de informar
        // qual Album deverá ser editado
        tela.putExtra("id", id);
        // é solicitado ao Android que seja iniciada a execução de uma nova Activity
        // porém também é solicitado informar quando esta activity retornar
        startActivityForResult(tela, EDITA_ALBUM);
    }

    /*
        Este método trata da ação de inclusão de um novo Album
     */
  /*  public void adicionaAlbum(View view) {
        // é criado um Intent para definir ao Android qual Activity será chamada
        Intent tela = new Intent(getBaseContext(), AddAlbumActivity.class);
        // é solicitado ao Android que seja iniciada a execução de uma nova Activity
        // porém também é solicitado informar quando esta activity retornar
        startActivityForResult(tela, NOVO_ALBUM);
    }*/

    /*
        Este método trata da ação do retorno de uma Activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String msg = "A ";
        switch (requestCode) {
            case 0:
                msg += "alteração do Album foi ";
                break;

            case 1:
                msg += "inclusão do Album foi ";
                break;

            case 2:
                msg += "ordenação da lista foi ";
                break;
        }

        // Se a activity informar que houve sucesso em sua execução
        if(resultCode == RESULT_OK) {
            // é solicitado ao Adapter que os itens da lista sejam atualizados no listView
            itemLista.notifyDataSetChanged();
            msg += "um sucesso";
        } else {
            msg += "cancelada";
        }
        Snackbar.make(listView, msg, Snackbar.LENGTH_LONG).show();
    }


    //Obtem a identificação da preferência para Ordenação


}
