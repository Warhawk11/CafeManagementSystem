package com.HustleCafe.rest;

import com.HustleCafe.model.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/category")
public interface CategoryRest {

    @PostMapping("/add")
    ResponseEntity<String> addNewCategory(@RequestBody(required = true)Map<String,String> requestMap);

    @GetMapping("/get")
    ResponseEntity<List<Category>> getAllCategory(@RequestParam(required = false) String filterValue);

    @PostMapping("/update")
    ResponseEntity<String> updateCategory(@RequestBody(required = true) Map<String,String> requestMap);
}

