package com.msa.stock.repository;

import com.msa.stock.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 상품 JPA 리포지토리.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
}
