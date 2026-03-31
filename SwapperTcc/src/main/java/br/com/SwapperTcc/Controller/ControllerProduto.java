package br.com.SwapperTcc.Controller;

import br.com.SwapperTcc.Dao.InterfaceProduto;
import br.com.SwapperTcc.Entity.Produto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@CrossOrigin("*")
@RequestMapping("/produtos")
public class ControllerProduto {

    @Autowired
    private InterfaceProduto dao;

    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping
    public ResponseEntity<?> listarProdutos() {
        return ResponseEntity.ok(dao.findAll());
    }

    @GetMapping("/produto/{id}")
    public ResponseEntity<Produto> getProdutoById(@PathVariable Integer id) {
        return dao.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Recebe FormData
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> criarProduto(
            @RequestParam("nome") String nome,
            @RequestParam("descricao") String descricao,
            @RequestParam("preco") BigDecimal preco,
            @RequestParam("categoria") String categoria,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "imagem", required = false) MultipartFile imagem
    ) {
        try {
            Produto produto = new Produto();
            produto.setNome(nome);
            produto.setDescricao(descricao);
            produto.setPreco(preco);
            produto.setCategoria(categoria);

            // Salva a imagem na pasta uploads se existir
            if (imagem != null && !imagem.isEmpty()) {
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                String filename = System.currentTimeMillis() + "_" + imagem.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR, filename);
                Files.write(filePath, imagem.getBytes());

                produto.setImagem(filename); // salva o nome do arquivo no DB
            }

            // Salva o produto
            Produto produtoSalvo = dao.save(produto);
            return ResponseEntity.ok(produtoSalvo);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Erro ao salvar a imagem: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Erro ao cadastrar produto: " + e.getMessage());
        }
    }
}
