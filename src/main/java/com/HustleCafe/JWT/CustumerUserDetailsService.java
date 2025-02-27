package com.HustleCafe.JWT;

import com.HustleCafe.dao.UserDao;
import com.HustleCafe.model.User;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Service
public class CustumerUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustumerUserDetailsService.class);
    @Autowired
    UserDao userDao;

    private com.HustleCafe.model.User userDetail;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Inside loadUserByUsername {}",username);
       userDetail = userDao.findByEmail(username);
       if(!Objects.isNull(userDetail)){
           return new org.springframework.security.core.userdetails.User(userDetail.getEmail(),userDetail.getPassword(),new ArrayList<>());
       }else{
           throw new UsernameNotFoundException("User Not Found");
       }
    }

    public User getUserDetail(){
        return userDetail;
    }
}
