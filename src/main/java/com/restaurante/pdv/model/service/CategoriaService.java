package com.restaurante.pdv.model.service;

import com.restaurante.pdv.dto.CategoriaDTO;
import com.restaurante.pdv.exception.NegocioException;
import com.restaurante.pdv.exception.RecursoNaoEncontradoException;
import com.restaurante.pdv.model.entity.Categoria;
import com.restaurante.pdv.model.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaDTO> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(CategoriaDTO::deEntidade)
                .collect(Collectors.toList());
    }

    public List<CategoriaDTO> buscarAtivas() {
        return categoriaRepository.findByAtivoTrue().stream()
                .map(CategoriaDTO::deEntidade)
                .collect(Collectors.toList());
    }

    public CategoriaDTO buscarPorId(Long id) {
        Categoria categoria = buscarEntidadePorId(id);
        return CategoriaDTO.deEntidade(categoria);
    }

    public Categoria buscarEntidadePorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }

    @Transactional
    public CategoriaDTO salvar(CategoriaDTO dto) {
        if (categoriaRepository.existsByNomeIgnoreCase(dto.getNome())) {
            throw new NegocioException(String.format("Já existe uma categoria cadastrada com o nome '%s'.", dto.getNome()));
        }

        Categoria categoria = dto.paraEntidade();
        categoria.setId(null);
        if (categoria.getAtivo() == null) {
            categoria.setAtivo(true);
        }

        Categoria salva = categoriaRepository.save(categoria);
        return CategoriaDTO.deEntidade(salva);
    }

    @Transactional
    public CategoriaDTO atualizar(Long id, CategoriaDTO dto) {
        Categoria categoria = buscarEntidadePorId(id);

        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(dto.getNome(), id)) {
            throw new NegocioException(String.format("Já existe outra categoria cadastrada com o nome '%s'.", dto.getNome()));
        }

        categoria.setNome(dto.getNome());
        categoria.setDescricao(dto.getDescricao());
        if (dto.getAtivo() != null) {
            categoria.setAtivo(dto.getAtivo());
        }

        Categoria atualizada = categoriaRepository.save(categoria);
        return CategoriaDTO.deEntidade(atualizada);
    }

    @Transactional
    public void desativar(Long id) {
        Categoria categoria = buscarEntidadePorId(id);
        categoria.setAtivo(false);
        categoriaRepository.save(categoria);
    }

    @Transactional
    public void reativar(Long id) {
        Categoria categoria = buscarEntidadePorId(id);
        categoria.setAtivo(true);
        categoriaRepository.save(categoria);
    }
}
