package com.HustleCafe.service;

import com.HustleCafe.model.Bill;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface BIllService {

    ResponseEntity<String> generateReport(Map<String,Object> requestMap);

    ResponseEntity<List<Bill>> getAllBills();

    ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap);

    ResponseEntity<String> deleteBill(Integer id);
}
