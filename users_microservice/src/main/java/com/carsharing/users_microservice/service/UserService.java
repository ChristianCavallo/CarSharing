package com.carsharing.users_microservice.service;

import com.carsharing.users_microservice.data.UserRepository;
import com.carsharing.users_microservice.entities.User;
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
        u.setRoles(Collections.singletonList("USER"));
        return repository.save(u);
    }


    public Page<User> getUsersPage(Integer page, Integer per_page) {
        Pageable paging = PageRequest.of(page, per_page);
        return repository.findAll(paging);
    }
}
