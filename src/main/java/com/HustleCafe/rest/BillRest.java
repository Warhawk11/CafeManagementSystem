package com.HustleCafe.rest;

import com.HustleCafe.model.Bill;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/bill")
public interface BillRest {


    @PostMapping("/generateReport")
    ResponseEntity<String> generateReport(@RequestBody Map<String, Object> requestMap);

    @GetMapping("/getAllBills")
    ResponseEntity<List<Bill>> getAllBills();

    @PostMapping("/getPdf")
    ResponseEntity<byte[]> getPdf(@RequestBody Map<String,Object> requestMap);

    @PostMapping("/delete/{id}")
    ResponseEntity<String> deleteBill(@PathVariable Integer id);


}
