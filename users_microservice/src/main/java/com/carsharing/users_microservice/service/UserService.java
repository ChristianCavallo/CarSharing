package com.carsharing.users_microservice.service;

import com.carsharing.users_microservice.data.UserRepository;
import com.carsharing.users_microservice.entities.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;


@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public Optional<User> getUserById(String id) {
        return repository.findById(id);

    }

    public Optional<User> getUserByEmail(String email) {
        return repository.findByEmail(email);
    }

    public User AddUser(User u) {
        u.getRoles().add("USER");
        u.setPassword(DigestUtils.sha256Hex(u.getPassword())); // Convert password to sha256 digest
        return repository.save(u);
    }


    public Page<User> getUsersPage(Integer page, Integer per_page) {
        Pageable paging = PageRequest.of(page, per_page);
        return repository.findAll(paging);
    }

    public Optional<User> getUserByEmailAndPassword(String email, String password){
        return repository.findByEmailAndPassword(email, DigestUtils.sha256Hex(password));
    }

    public User initializeAdminUser(User u){
        if(repository.findById(u.getId()).isPresent()){
            System.out.println("Administrator user already exists. Skipping creation.");
            return null;
        }

        u = AddUser(u);
        System.out.println("New admin created.");
        System.out.println(u);
        return u;
    }
}
