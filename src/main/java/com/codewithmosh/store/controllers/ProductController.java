package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.ProductDto;
import com.codewithmosh.store.entities.Product;
import com.codewithmosh.store.mappers.ProductMapper;
import com.codewithmosh.store.repositories.CategoryRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public List<ProductDto> getProducts(
            @RequestParam(name = "categoryId", required = false) Byte categoryId
    ) {
        List<Product> products;
        if (categoryId == null) {
            products = productRepository.findAllWithCategory();
        } else {
            products = productRepository.findByCategoryId(categoryId);
        }

        return products.stream().map(productMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        var product = productRepository.findById(id).orElse(null);

        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(productMapper.toDto(product));
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(
            @RequestBody ProductDto request,
            UriComponentsBuilder uriBuilder
    ) {
        var categoryId = request.getCategoryId();
        var category = categoryRepository.findById(categoryId).orElse(null);

        var product = productMapper.toEntity(request);
        product.setCategory(category);
        productRepository.save(product);

        var dto = productMapper.toDto(product);
        var uri = uriBuilder.path("/products/{id}").buildAndExpand(dto.getId()).toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable(name = "id") Long id,
            @RequestBody ProductDto request
    ) {
        var product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        var categoryId = request.getCategoryId();
        if (categoryId == null) {
            return ResponseEntity.notFound().build();
        }

        var category = product.getCategory();
        if (category != null && categoryId != category.getId()) {
            product.setCategory(categoryRepository.findById(categoryId).orElse(null));
        }
        productMapper.update(request, product);
        productRepository.save(product);

        return ResponseEntity.ok(productMapper.toDto(product));
    }
}
