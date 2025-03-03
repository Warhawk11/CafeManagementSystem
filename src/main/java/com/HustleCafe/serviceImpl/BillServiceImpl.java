package com.HustleCafe.serviceImpl;

import com.HustleCafe.JWT.JwtFilter;
import com.HustleCafe.constants.CafeConstants;
import com.HustleCafe.dao.BillDao;
import com.HustleCafe.model.Bill;
import com.HustleCafe.service.BIllService;
import com.HustleCafe.utils.CafeUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class BillServiceImpl implements BIllService {
    private static final Logger log = LoggerFactory.getLogger(BillServiceImpl.class);

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    BillDao billDao;

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        log.info("Inside generateReport");
        try {
            String fileName;

            if (validateRequestMap(requestMap)) {
                // Fix: Accept "fileName" if provided; otherwise, generate "uuid"
                if (requestMap.containsKey("isGenerate") && !(Boolean) requestMap.get("isGenerate")) {
                    fileName = (String) requestMap.get("fileName");
                } else {
                    fileName = CafeUtils.getUUID();
                    requestMap.put("uuid", fileName);
                    insertBill(requestMap);
                }

                String data = "Name: " + requestMap.get("name") + "\n"
                        + "Contact Number: " + requestMap.get("contactNumber") + "\n"
                        + "Email: " + requestMap.get("email") + "\n"
                        + "Payment Method: " + requestMap.get("paymentMethod");

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(CafeConstants.STORE_LOCATION + "\\" + fileName + ".pdf"));
                document.open();
                setRectangleInPdf(document);

                Paragraph chunk = new Paragraph("Hustle Cafe", getFont("Header"));
                chunk.setAlignment(Element.ALIGN_CENTER);
                document.add(chunk);

                Paragraph paragraph = new Paragraph(data + "\n\n", getFont("Data"));
                document.add(paragraph);

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addTableHeader(table);

                // Fix: Properly parse productDetails JSON array
                JSONArray jsonArray = new JSONArray(requestMap.get("productDetails").toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    addRow(table, jsonArray.getJSONObject(i));
                }
                document.add(table);

                Paragraph footer = new Paragraph("Total: " + requestMap.get("totalAmount") + "\n" +
                        "Thank you for visiting. Please visit again!", getFont("Data"));
                document.add(footer);

                document.close();
                return new ResponseEntity<>("{\"uuid\":\"" + fileName + "\"}", HttpStatus.OK);
            } else {
                return CafeUtils.getResponseEntity("Bill not valid", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void addRow(PdfPTable table, JSONObject data) throws JSONException {
        log.info("Inside addRow");
        table.addCell(data.getString("name"));
        table.addCell(data.getString("category"));
        table.addCell(data.getString("quantity"));
        table.addCell(String.valueOf(data.getDouble("price")));
        table.addCell(String.valueOf(data.getDouble("total"))); // Fix: Correct field name
    }

    private void addTableHeader(PdfPTable table) {
        log.info("Inside tableHeader");
        Stream.of("Name", "Category", "Quantity", "Price", "Subtotal").forEach(columnTitle -> {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(BaseColor.YELLOW);
            header.setBorderWidth(2);
            header.setPhrase(new Phrase(columnTitle));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(header);
        });
    }

    private Font getFont(String type) {
        log.info("Inside getFont");
        switch (type) {
            case "Header":
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18, BaseColor.BLACK);
                headerFont.setStyle(Font.BOLD);
                return headerFont;
            case "Data":
                Font dataFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
                dataFont.setStyle(Font.BOLD);
                return dataFont;
            default:
                return new Font();
        }
    }

    private void setRectangleInPdf(Document document) throws DocumentException {
        log.info("Inside setRectangleInPdf");
        Rectangle rectangle = new Rectangle(577, 825, 18, 15);
        rectangle.setBorderColor(BaseColor.BLACK);
        rectangle.setBorderWidth(1);
        document.add(rectangle);
    }

    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setContactNumber((String) requestMap.get("contactNumber"));
            bill.setEmail((String) requestMap.get("email"));
            bill.setPaymentMethod((String) requestMap.get("paymentMethod"));

            // Fix: Convert "totalAmount" properly
            bill.setTotalAmount(Integer.parseInt(requestMap.get("totalAmount").toString()));

            bill.setProductDetail(requestMap.get("productDetails").toString());
            bill.setCreatedBy(jwtFilter.getCurrentUser());
            billDao.save(bill);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean validateRequestMap(Map<String, Object> requestMap) {
        log.info("Inside validateRequestMap");
        return requestMap.containsKey("name") &&
                requestMap.containsKey("contactNumber") &&
                requestMap.containsKey("email") &&
                requestMap.containsKey("paymentMethod") &&
                requestMap.containsKey("productDetails") &&
                requestMap.containsKey("totalAmount");
    }

    @Override
    public ResponseEntity<List<Bill>> getAllBills() {
        List<Bill> list = new ArrayList<>();
        if(jwtFilter.isAdmin()){
            list=billDao.getAllBills();
        }else{
            list = billDao.getBillByUsername(jwtFilter.getCurrentUser());
        }
        return new ResponseEntity<>(list,HttpStatus.OK);
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("Inside getPdf", requestMap);
        try {
            byte[] byteArray = new byte[0];
            if(!requestMap.containsKey("uuid")&&validateRequestMap(requestMap))
                return new ResponseEntity<>(byteArray,HttpStatus.BAD_REQUEST);
            String filepath = CafeConstants.STORE_LOCATION+"\\"+(String)requestMap.get("uuid")+".pdf";
            if(CafeUtils.isFileExist(filepath)){
                byteArray =getByteArray(filepath);
                return new ResponseEntity<>(byteArray,HttpStatus.OK);
            }else{
                requestMap.put("isGenerate",false);
                generateReport(requestMap);
                byteArray=getByteArray(filepath);
                return new ResponseEntity<>(byteArray,HttpStatus.OK);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    private byte[] getByteArray(String filepath) throws Exception{
        File initialFile = new File(filepath);
        InputStream targetStream = new FileInputStream(initialFile);
        byte[] byteArray = IOUtils.toByteArray(targetStream);
        targetStream.close();
        return byteArray;
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {

        try {
            Optional<Bill> optional = billDao.findById(id);
            if(optional.isPresent()){
                billDao.deleteById(id);
                return CafeUtils.getResponseEntity("Bill Deleted Successfully",HttpStatus.OK);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }


        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
