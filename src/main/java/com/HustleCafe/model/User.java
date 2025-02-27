package com.HustleCafe.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.ArrayList;

@Data
@Entity
@DynamicInsert
@DynamicUpdate
@NamedQuery(name = "User.findByEmail", query = "select u from User u where u.email = :email")
@NamedQuery(name = "User.getAllUser", query = "select new com.HustleCafe.wrapper.UserWrapper(u.id,u.name,u.email,u.contactNumber,u.status) from User u where u.role = 'user'")
@NamedQuery(name = "User.getAllAdmin", query = "select u.email from User u where u.role = 'admin'")
@NamedQuery(name = "User.updateStatus", query = "update User u set u.status =: status where u.id =:id ")
@Table(name = "cafe_user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = " name")
    private String name;

    @Column(name = " contactNumber")
    private String contactNumber;

    @Column(name = " email")
    private String email;

    @Column(name = " password")
    private String password;

    @Column(name = " status")
    private String status;

    @Column(name = " role")
    private String role;

    public User() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
