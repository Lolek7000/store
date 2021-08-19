package com.karold.onlinestore.service.impl;

import com.karold.onlinestore.exception.IllegalPriceException;
import com.karold.onlinestore.exception.NegativeProductQuantityException;
import com.karold.onlinestore.exception.ProductAlreadyExistsException;
import com.karold.onlinestore.exception.ProductNotFoundException;
import com.karold.onlinestore.model.Product;
import com.karold.onlinestore.repository.ProductRepository;
import com.karold.onlinestore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

public class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    @Override
    public Product addProduct(Product product) {
        return (Product) productRepository.findByName(product.getName()).map(dbProduct -> {
            throw new ProductAlreadyExistsException(product.getName());
        }).orElse(productRepository.save(product));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> {
            throw new ProductNotFoundException(productId);
        });
    }

    @Override
    public Product updateProductQuantity(Long productId, int quantity) {
        Product product = productRepository.findById(productId).orElseThrow(() -> {
            throw new ProductNotFoundException(productId);
        });
        if (quantity >= 0) {
            product.setQuantity(quantity);
            return productRepository.save(product);
        } else {
            throw new NegativeProductQuantityException();
        }
    }

    @Override
    public Product updateProductPrice(Long productId, BigDecimal price) {
        Product product = productRepository.findById(productId).orElseThrow(() -> {
            throw new ProductNotFoundException(productId);
        });
        if (price.compareTo(BigDecimal.valueOf(0.0)) > 0) {
            product.setPrice(price);
            return productRepository.save(product);
        }
        throw new IllegalPriceException();
    }

    @Override
    public boolean deleteProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> {
            throw new ProductNotFoundException(productId);
        });
        productRepository.delete(product);
        return true;
    }
}
