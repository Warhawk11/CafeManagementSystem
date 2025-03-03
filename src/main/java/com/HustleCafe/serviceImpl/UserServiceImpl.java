package com.HustleCafe.serviceImpl;

import com.HustleCafe.JWT.CustumerUserDetailsService;
import com.HustleCafe.JWT.JwtFilter;
import com.HustleCafe.JWT.JwtUtil;
import com.HustleCafe.constants.CafeConstants;
import com.HustleCafe.dao.UserDao;
import com.HustleCafe.model.User;
import com.HustleCafe.service.UserService;
import com.HustleCafe.utils.CafeUtils;
import com.HustleCafe.utils.EmailUtils;
import com.HustleCafe.wrapper.UserWrapper;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustumerUserDetailsService custumerUserDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Add PasswordEncoder here

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        try {
            if (validateSignUpMap(requestMap)) {
                User user = userDao.findByEmail(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    // Encrypt password before saving
                    String encodedPassword = passwordEncoder.encode(requestMap.get("password"));
                    userDao.save(getUserFromMap(requestMap, encodedPassword)); // Pass encoded password
                    return CafeUtils.getResponseEntity("Successfully Registered", HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity("Email already Exists", HttpStatus.BAD_REQUEST);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private User getUserFromMap(Map<String, String> requestMap, String encodedPassword) {
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(encodedPassword); // Set the encoded password
        user.setStatus("false");
        user.setRole("user");
        return user;
    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        return requestMap.containsKey("name") && requestMap.containsKey("contactNumber") &&
                requestMap.containsKey("email") && requestMap.containsKey("password");
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password"))
            );
            if (auth.isAuthenticated()) {
                if (custumerUserDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")) {
                    return new ResponseEntity<String>(
                            "{\"token\" : \"" + jwtUtil.generateToken(custumerUserDetailsService.getUserDetail().getEmail(),
                                    custumerUserDetailsService.getUserDetail().getRole()) + "\"}", HttpStatus.OK
                    );
                } else {
                    return new ResponseEntity<String>(
                            "{\"message\" : \"Wait for admin approval.\"}", HttpStatus.BAD_REQUEST
                    );
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<String>("{\"message\" : \"Bad Credentials.\"}", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
            if(jwtFilter.isAdmin()){
                return new ResponseEntity<>(userDao.getAllUser(),HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ArrayList<>(),HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
                Optional<User> optional= userDao.findById(Integer.parseInt(requestMap.get("id")));
                if(optional.isPresent()){
                    userDao.updateStatus(requestMap.get("status"),Integer.parseInt(requestMap.get("id")));
                    sendMailToAllAdmin(requestMap.get("status"),optional.get().getEmail(),userDao.getAllAdmin());
                    return CafeUtils.getResponseEntity("User Status Updated ", HttpStatus.OK);
                }else{
                    return CafeUtils.getResponseEntity("User Id doesnt Exist",HttpStatus.OK);
                }
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity("yayayya",HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtFilter.getCurrentUser());
        if(status!=null && status.equalsIgnoreCase("true")){
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Approved" , "USER : "+ user + "\n is aprroved by \nADMIN:-" + jwtFilter.getCurrentUser(),allAdmin);
        }else{
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Disabled" , "USER : "+ user + "\n is disabled by \nADMIN:-" + jwtFilter.getCurrentUser(),allAdmin);
        }
    }

    @Override
    public ResponseEntity<String> checkToken() {
        return CafeUtils.getResponseEntity("true", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try {
            User userObj = userDao.findByEmail(jwtFilter.getCurrentUser());
            if (userObj != null) {
                // Check if the old password matches the hashed password in DB
                if (passwordEncoder.matches(requestMap.get("oldPassword"), userObj.getPassword())) {
                    // Encode the new password before saving
                    userObj.setPassword(passwordEncoder.encode(requestMap.get("newPassword")));
                    userDao.save(userObj);
                    return CafeUtils.getResponseEntity("Password Updated Successfully", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity("Incorrect Old Password", HttpStatus.BAD_REQUEST);
            }
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try {
            User user = userDao.findByEmail(requestMap.get("email"));
            if(!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail())){
                emailUtils.forgotMail(user.getEmail(),"Credentials by Cafe Management System", user.getPassword());
                return CafeUtils.getResponseEntity("abcd",HttpStatus.OK);
            }
            return CafeUtils.getResponseEntity("Check your mail for credentials",HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
