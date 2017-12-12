package com.fernando.albuns.model.bean;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by 01796085090 on 23/11/2017.
 */

public class Album implements Comparable<Album> {
    private Long id;
    private String banda;
    private String album;
    private String genero;
    private Date lancamento;
    // Atributo para identificar que o Album esta marcado para exclusão
    private boolean del;
    private Bitmap bitmap;
    private static DateFormat fmt = DateFormat.getDateInstance(DateFormat.LONG);

    public Album() {
    }

    // Implementado para o uso do método indexOf na classe AlbumDao
    public Album(Long id) {
        this.id = id;
    }

    public Album(Long id, String banda, String album, String genero, Date lancamento) {
        this.id = id;
        this.banda = banda;
        this.album = album;
        this.genero = genero;
        this.lancamento = lancamento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBanda() {
        return banda;
    }

    public void setBanda(String banda) {
        this.banda = banda;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Date getLancamento() {
        return lancamento;
    }
    public String getLancamentoFmt() {
        return fmt.format(lancamento);
    }

    public void setLancamento(Date lancamento) {
        this.lancamento = lancamento;
    }

    public boolean isDel() {
        return del;
    }

    public void setDel(boolean del) {
        this.del = del;
    }

    // Implementado para o uso do método indexOf na classe AlbumDao
    // estes métodos são necessários para que as pesquisas e teste
    // de igualdade funcionem adequadamente quando esta classe for
    // utilizada em Collections tais como List, Set e Map


    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album album = (Album) o;

        if (!id.equals(album.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(@NonNull Album outro) {
        return banda.compareToIgnoreCase(outro.banda);
    }
}
