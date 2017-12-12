package com.fernando.albuns.model.dao;




import com.fernando.albuns.model.bean.Album;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by 01796085090 on 23/11/2017.
 */

public class AlbumDao {
    // Mantém a única instância de objeto do AlbumDao
    // para possibilitar que a lista de albuns seja
    // única na aplicação
    public static AlbumDao manager = new AlbumDao();
    // Lista onde serão armazenados os objetos Albuns
    private List<Album> lista;
    // atributo utilizado para a geração do ID para cada
    // novo Album
    private long id = 0;

    // Construtor que inicializa a lista
    // e temporariamente inicializa dois objetos Album
    // para teste da aplicação
    private AlbumDao() {
        lista = new ArrayList<>();
        lista.add(new Album(id++, "The Beatles", "Sgt. Pepper's Lonely Hearts Club Band", "Rock", new GregorianCalendar(1967, Calendar.MAY, 26).getTime()));
        lista.add(new Album(id++, "AC/DC", "Highway to Hell", "Hard Rock", new GregorianCalendar(1979, Calendar.JULY, 27).getTime()));
    }

    /*
        método utilizado para a obtenção de uma lista ordenada de Albuns
     */

    public List<Long> listarIds(String ordem) {
        if (ordem.equals("Banda")) {
            Collections.sort(lista);
        } else if (ordem.equals("Album")) {
            Collections.sort(lista, new OrdenaPorAlbum());
        } else {
            Collections.sort(lista, new OrdenaPorLancamento());
        }

        List<Long> ids = new ArrayList<>();
        for(Album obj : lista)
            ids.add(obj.getId());
        return ids;
    }


    /*public List<Album> getLista() {
        // Todas aas versões de Java
        Collections.sort(lista);
        // suportado no Java 8
        // Collections.sort(lista, Comparator.comparing(Album::getAlbum));
        return Collections.unmodifiableList(lista);
        // suportado em todas as versões do Java
        // return Collections.synchronizedList(lista);
    }*/

    /*
        médoto utilizado para a localização de um objeto album a partir de seu ID
     */
    public Album getAlbum(final Long id){
        // Pesquisa tradicional
        Album oAlbum = null;
        for(Album obj : lista) {
            if(obj.getId() == id) {
                oAlbum = obj;
                break;
            }

        }

        // Pesquisa utilizando o recurso de Collections
        // necessita que a classe tenha construtor especializado
        // além da implementação dos métodos equals e hashcode
        Album albumLocalizado =
                lista.get(lista.indexOf(new Album(id)));

        // Utiliza a implementação funcional em Java 8
//        Album outroAlbum = lista.stream()
//                .filter(obj -> obj.getId() == id)
//                .findAny().orElse(null);

        return oAlbum;
    }

    /*
        método utilizado para incluir ou atualizar um album na lista interna
     */
    public void salvar(Album obj) {
        // Se o objeto não tem ID é reconhecido como
        // novo objeto a ser incluido na lista
        if(obj.getId() == null) {
            obj.setId(id++);
            lista.add(obj);
        } else {
            // caso contrário é efetuada pesquisa
            // para localizar o objeto antigo na lista
            // pelo id para que seja substituido
            int posicao = lista.indexOf(new Album(obj.getId()));
            lista.set(posicao, obj);
        }
    }

    /*
        médoto utilizado para remover um determinado Album a partir de seu ID
     */
    public void remover(Long id) {
        // Localiza o objeto pelo id informado
        // e em seguida remove da lista
        lista.remove(new Album(id));
    }

    /*
        médoto utilizado para remover todos os jogos selecionados para exclusão
     */
    public void apagarOsSelecionados() {
        // Constroi a lista dos Albuns a serem removidos
        List<Album> osAlbuns = new ArrayList<>();
        for(Album obj : lista) {
            if(obj.isDel()) {
                osAlbuns.add(obj);
            }
        }

        // Remove todos os Albuns da lista dos excluiveis
        for(Album album : osAlbuns) {
            remover(album.getId());
        }
    }

    public boolean temAlbumPraApagar() {
        boolean temAlbumSelecionado = false;
        for(Album obj : lista) {
            if(obj.isDel()) {
                temAlbumSelecionado = true;
                break;
            }
        }
        return temAlbumSelecionado;
    }

    class OrdenaPorAlbum implements Comparator<Album> {
        @Override
        public int compare(Album o1, Album o2) {
            return o1.getAlbum().compareToIgnoreCase(o2.getAlbum());
        }
    }

    class OrdenaPorLancamento implements Comparator<Album> {
        @Override
        public int compare(Album o1, Album o2) {
            return o1.getLancamentoFmt().compareTo(o2.getLancamentoFmt());
        }
    }
}
