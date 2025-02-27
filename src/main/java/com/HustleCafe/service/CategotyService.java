package com.HustleCafe.service;

import com.HustleCafe.model.Category;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CategotyService {
    ResponseEntity<String> addNewCategory(Map<String,String> requestMap);
    ResponseEntity<List<Category>> getAllCategory(String filterValue);
    ResponseEntity<String> updateCategory( Map<String, String> requestMap);
}
