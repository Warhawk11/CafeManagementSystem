package com.HustleCafe.serviceImpl;

import com.HustleCafe.JWT.JwtFilter;
import com.HustleCafe.constants.CafeConstants;
import com.HustleCafe.dao.ProductDao;
import com.HustleCafe.model.Category;
import com.HustleCafe.model.Product;
import com.HustleCafe.service.ProductService;
import com.HustleCafe.utils.CafeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    ProductDao productDao;

    @Override
    public ResponseEntity<String> addNewProduct(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()){
                if(validateProductMap(requestMap,false)){
                    productDao.save(getProductMap(requestMap,false));
                }
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA,HttpStatus.BAD_REQUEST);
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Product getProductMap(Map<String, String> requestMap, boolean isAdd) {
        Category category = new Category();

        category.setId(Integer.parseInt(requestMap.get("id")));

        Product product = new Product();

        if(isAdd){
            product.setId(Integer.parseInt(requestMap.get("id")));
        }else{
            product.setStatus("true");
        }
        product.setCategory(category);
        product.setName(requestMap.get("name"));
        product.setDescription(requestMap.get("description"));
        product.setPrice(Integer.parseInt(requestMap.get("price")));
        return product;

    }

    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        if(requestMap.containsKey("name") ) {
            if (requestMap.containsKey("id") && validateId) {
                return true;
            } else if (!validateId) {
                return true;
            }
        }
        return false;
    }
}
