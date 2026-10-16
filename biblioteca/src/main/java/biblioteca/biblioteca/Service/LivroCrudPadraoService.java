package biblioteca.biblioteca.Service;

import biblioteca.biblioteca.Dto.AutorResponseDto;
import biblioteca.biblioteca.Dto.LivroPostPutDto;
import biblioteca.biblioteca.Dto.LivroResponseDto;
import biblioteca.biblioteca.Model.Autor;
import biblioteca.biblioteca.Model.Livro;
import biblioteca.biblioteca.Repository.AutorRepository;
import biblioteca.biblioteca.Repository.LivroRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LivroCrudPadraoService implements LivroCrudService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    LivroRepository livroRepository;

    @Autowired
    AutorRepository autorRepository;

    @Override
    public LivroResponseDto criarLivro(LivroPostPutDto livroPostPutDto) {
        if (livroRepository.existsByIsbn(livroPostPutDto.getIsbn())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um livro com esse ISBN");
        }
        Livro livro = Livro.builder()
                .isbn(livroPostPutDto.getIsbn())
                .titulo(livroPostPutDto.getTitulo())
                .ano(livroPostPutDto.getAno())
                .edicao(livroPostPutDto.getEdicao())
                .autor(obterAutorExistente(livroPostPutDto.getAutorId()))
                .build();
        return paraResponse(livroRepository.save(livro));
    }

    @Override
    public List<LivroResponseDto> buscarLivros(String isbn, String titulo) {
        List<Livro> livros;
        if (isbn != null && !isbn.isBlank()) {
            livros = livroRepository.findByIsbnContainingIgnoreCase(isbn);
        } else if (titulo != null && !titulo.isBlank()) {
            livros = livroRepository.findByTituloContainingIgnoreCase(titulo);
        } else {
            livros = livroRepository.findAll();
        }
        return livros.stream().map(this::paraResponse).toList();
    }

    @Override
    public LivroResponseDto buscarLivro(Long id) {
        return paraResponse(obterLivroExistente(id));
    }

    @Override
    public LivroResponseDto atualizarLivro(Long id, LivroPostPutDto livroPostPutDto) {
        Livro livro = obterLivroExistente(id);
        if (livroRepository.existsByIsbnAndIdNot(livroPostPutDto.getIsbn(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um livro com esse ISBN");
        }
        livro.setIsbn(livroPostPutDto.getIsbn());
        livro.setTitulo(livroPostPutDto.getTitulo());
        livro.setAno(livroPostPutDto.getAno());
        livro.setEdicao(livroPostPutDto.getEdicao());
        livro.setAutor(obterAutorExistente(livroPostPutDto.getAutorId()));
        return paraResponse(livroRepository.save(livro));
    }

    @Override
    public void removerLivro(Long id) {
        livroRepository.delete(obterLivroExistente(id));
    }

    private LivroResponseDto paraResponse(Livro livro) {
        return LivroResponseDto.builder()
                .id(livro.getId())
                .isbn(livro.getIsbn())
                .titulo(livro.getTitulo())
                .ano(livro.getAno())
                .edicao(livro.getEdicao())
                .autor(modelMapper.map(livro.getAutor(), AutorResponseDto.class))
                .build();
    }

    private Livro obterLivroExistente(Long id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));
    }

    private Autor obterAutorExistente(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Autor não encontrado"));
    }
}