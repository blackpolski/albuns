package com.fernando.albuns.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.util.SparseLongArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.fernando.albuns.R;
import com.fernando.albuns.model.bean.Album;
import com.fernando.albuns.model.dao.AlbumDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 01796085090 on 23/11/2017.
 */
enum TipoDeDetalhe {
    EDICAO,
    EXCLUSAO;
}

@SuppressLint("UseSparseArrays")
class AlbumAdapter extends BaseAdapter implements View.OnClickListener {
    private AlbumDao dao = AlbumDao.manager;
    private SparseLongArray mapa;
    private boolean trocouLayout = false;
    private boolean apagar = false;
    private Activity activity;


    public AlbumAdapter(Activity activity) {
        this.activity = activity;
        criaMapa();
    }

    @Override
    public void notifyDataSetChanged() {
        criaMapa();
        super.notifyDataSetChanged();
    }

    private void criaMapa() {

        // Obtém a identificação da preferência para Ordenação
        String ordemPreference = activity.getResources().getString(R.string.ordem_key);
        // Obtém o valor padrão para a Ordenação
        String ordemDefault = activity.getResources().getString(R.string.ordem_default);
        // Obtém o recurso de leitura de preferências
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        // Localiza a configuração selecionada para Ordenação de Albuns
        String ordem = preferences.getString(ordemPreference, ordemDefault);

        mapa = new SparseLongArray();
        List<Long> ids = dao.listarIds(ordem);
        for (int linha = 0; linha < ids.size(); linha++){
            mapa.put(linha, ids.get(linha));

        /*// Cria o mapa de associação de linha:id
        mapa = new HashMap<>();
        // Obtem a lista de Objetos do DAO
        List<Album> lista =  dao.getLista();

        // Associa o nº da linha com o id do Album
        for(int linha = 0;linha < lista.size();linha++) {
            Album album = lista.get(linha);
            mapa.put(linha, album.getId());
        */}
    }

    // Este método é responsável por trocar o
    // layout do detalhe da lista e
    // notificar o listView da mudança
    public void trocouOLayout(TipoDeDetalhe tipo) {
        if(tipo == TipoDeDetalhe.EDICAO) {
            trocouLayout = true;
            apagar = false;
        } else {
            trocouLayout = true;
            apagar = true;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mapa.size();
    }

    @Override
    public Object getItem(int id) {
        return dao.getAlbum((long)id);
    }

    @Override
    public long getItemId(int linha) {
        return mapa.get(linha);
    }

    @Override
    public View getView(int linha, View view, ViewGroup viewGroup) {

        ConstraintLayout layout;
        if(view == null || trocouLayout) {
            // Obtem o contexto de execução do ListView
            Context context = viewGroup.getContext();
            // localizar o serviço de construção do layout
            LayoutInflater inflater =
                    (LayoutInflater)context
                            .getSystemService(
                                    Context.LAYOUT_INFLATER_SERVICE);
            // Criar um objeto de Layout
            layout = new ConstraintLayout(context);
            // informar o layout xml a ser carregado
            if(!apagar) {
                inflater.inflate(R.layout.item_list_editar, layout);
            } else {
                inflater.inflate(R.layout.item_list_excluir, layout);
            }
        } else {
            layout = (ConstraintLayout)view;
        }

        // o registro da posição solicitada e encontrar o objeto
        // atribuir o objeto ao layout
        TextView banda = (TextView) layout.findViewById(R.id.lBanda);
        TextView album = (TextView) layout.findViewById(R.id.lAlbum);
        TextView genero = (TextView) layout.findViewById(R.id.lGenero);
        TextView lancamento = (TextView) layout.findViewById(R.id.lLancamento);
        ImageView fotoAlbum = layout.findViewById(R.id.iv_foto_album_main);

        Long id = mapa.get(linha);
        Album album1 = dao.getAlbum(id);

        banda.setText(album1.getBanda());
        album.setText(album1.getAlbum());
        genero.setText(album1.getGenero());
        lancamento.setText(album1.getLancamentoFmt());
        fotoAlbum.setImageBitmap(album1.getBitmap());

        // Lógica do Alex...

        if (album1.getBitmap() != null){
            fotoAlbum.setImageBitmap(album1.getBitmap());
        }else{
            fotoAlbum.setImageResource(R.drawable.disco_vinil);
        }


        // Cria um checkBox e registrar o evento de click
        // este evento marcará o Album (pelo ID) para exclusão

        if(apagar) {
            CheckBox checkBox = layout.findViewById(R.id.checkBox);
            checkBox.setTag(album1.getId());
            checkBox.setOnClickListener(this);
        }

        return layout;
    }

    @Override
    public void onClick(View view) {
        Long id = (Long)view.getTag();
        Album album = dao.getAlbum(id);
        album.setDel(!album.isDel());

        Log.d("AlbumAdapter", "Album marcado para exclusão [" +
                album.isDel() +
                "] id: " + album.getAlbum());


            dao.salvar(album);
        }
}

