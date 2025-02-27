package com.HustleCafe.rest;

import com.HustleCafe.wrapper.UserWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@RequestMapping("/user")
public interface UserRest {

    @PostMapping("/signup")
    ResponseEntity<String> signUp(@RequestBody Map<String, String> requestMap);

    @PostMapping("/login")
    ResponseEntity<String> login(@RequestBody Map<String, String> requestMap);

    @GetMapping("/get")
    ResponseEntity<List<UserWrapper>> getAllUser();

    @PostMapping("/update")
    ResponseEntity<String> update(@RequestBody(required = true)Map<String,String> requestMap);

    @GetMapping("/checkToken")
    ResponseEntity<String> checkToken();

    @PostMapping("/changePassword")
    ResponseEntity<String> changePassword(@RequestBody Map<String,String> requestMap);

    @PostMapping("/forgotPassword")
    ResponseEntity<String> forgotPassword(@RequestBody Map<String,String> requestMap);
}
