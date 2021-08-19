package com.karold.onlinestore;

import com.karold.onlinestore.exception.NegativeProductQuantityException;
import com.karold.onlinestore.exception.ProductAlreadyExistsException;
import com.karold.onlinestore.exception.ProductNotFoundException;
import com.karold.onlinestore.model.Product;
import com.karold.onlinestore.repository.ProductRepository;
import com.karold.onlinestore.service.impl.ProductServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.groups.Default;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();

    @Test
    public void addProduct_CorrectProductGiven_ReturnAddedProduct() {
        Product product = new Product(1l, "Samsung Galaxy s10", BigDecimal.valueOf(2744.00), 27);
        when(productRepository.save(product)).thenReturn(product);
        Product result = productService.addProduct(product);
        Assert.assertEquals(product, result);
    }

    @Test
    public void addProduct_ProductWithAlreadyExistingNameGiven_ShouldThrowProductAlreadyExist() {
        Product product = new Product(1l, "Samsung Galaxy s10", BigDecimal.valueOf(2744.00), 32);
        when(productRepository.findByName("Samsung Galaxy s10")).thenReturn(Optional.of(product));
        Assert.assertThrows(ProductAlreadyExistsException.class, () -> {
            productService.addProduct(new Product(2l, "Samsung Galaxy s10", BigDecimal.valueOf(1457.00), 27));
        });
    }

    @Test
    public void addProduct_ProductWithNegativeQuantityGiven_ShouldThrowConstraintViolationException() {
        validator.afterPropertiesSet();
        Product product = new Product(1l, "Samsung Galaxy s10", BigDecimal.valueOf(2744.00), -27);
        Set<ConstraintViolation<Product>> violations = validator.validate(product, Default.class);
        when(productRepository.save(any(Product.class))).thenThrow(new ConstraintViolationException(violations));
        Assert.assertThrows(ConstraintViolationException.class, () -> productService.addProduct(product));
        Assert.assertEquals(1, violations.size());

    }

    @Test
    public void getAllProduct_ShouldReturnListOfProduct() {
        List<Product> products = new ArrayList<>();
        products.add(new Product(1l, "Samsung Galaxy s10", BigDecimal.valueOf(1744.00), 27));
        products.add(new Product(2l, "Asus Zenfone 8", BigDecimal.valueOf(2684.00), 31));
        products.add(new Product(3l, "Lenovo Legion Y520-15 i5-7300HQ/8GB/1000 GTX1050", BigDecimal.valueOf(3449.00), 17));
        when(productRepository.findAll()).thenReturn(products);
        Assert.assertEquals(products, productService.getAllProducts());
    }

    @Test
    public void getProduct_NotExistingProductIdGiven_ShouldThrowProductNotFoundException() {
        when(productRepository.findById(1l)).thenReturn(Optional.empty());
        Assert.assertThrows(ProductNotFoundException.class, () -> {
            productService.getProductById(1l);
        });
    }

    @Test
    public void getProduct_ExistingProductIdGiven_ShouldReturnProductWithGivenId() {
        Product productFromDB = new Product(1l, "Samsung Galaxy s10", BigDecimal.valueOf(2744.00), 27);
        when(productRepository.findById(1l)).thenReturn(Optional.of(productFromDB));
        Product result = productService.getProductById(1l);
        Assert.assertEquals(productFromDB, result);
    }

    @Test
    public void updateProductQuantity_ShouldReturnProductWithUpdatedQuantity() {
        Product product = new Product(1l, "Samsung Galaxy s10", BigDecimal.valueOf(2744.00), 21);
        when(productRepository.findById(1l)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        Product updatedProduct = productService.updateProductQuantity(1l, 29);
        Assert.assertEquals(29, updatedProduct.getQuantity());
    }

    @Test
    public void updateProductQuantity_NegativeQuantityGiven_ShouldThrowException() {
        Product product = new Product(1l, "Samsung Galaxy s10", BigDecimal.valueOf(2744.00), 27);
        when(productRepository.findById(1l)).thenReturn(Optional.of(product));
        Assert.assertThrows(NegativeProductQuantityException.class, () -> productService.updateProductQuantity(1l, -12));

    }
}
