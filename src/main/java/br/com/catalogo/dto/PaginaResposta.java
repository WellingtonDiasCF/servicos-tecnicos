package br.com.catalogo.dto;

import java.util.List;

public class PaginaResposta<T> {
    public List<T> itens;
    public int paginaAtual;
    public int totalPaginas;
    public long totalRegistros;

    public PaginaResposta(List<T> itens, int paginaAtual, int totalPaginas, long totalRegistros) {
        this.itens = itens;
        this.paginaAtual = paginaAtual;
        this.totalPaginas = totalPaginas;
        this.totalRegistros = totalRegistros;
    }
}
