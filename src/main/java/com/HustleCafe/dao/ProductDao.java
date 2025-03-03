package com.HustleCafe.dao;

import com.HustleCafe.model.Product;
import com.HustleCafe.wrapper.ProductWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface ProductDao extends JpaRepository<Product,Integer> {
    List<ProductWrapper> getAllProduct();

    @Modifying
    @Transactional
    Integer updateProductStatus(@Param("status") String status, @Param("id") Integer id);

    List<ProductWrapper> getByCategory(@Param("id") Integer id);

    ProductWrapper getByProductId(@Param("id") Integer id);
}
