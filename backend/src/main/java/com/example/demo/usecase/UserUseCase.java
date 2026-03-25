package com.example.demo.usecase;

import com.example.demo.domain.User;
import java.util.List;

public interface UserUseCase {
    User getById(Long id);
    User getCurrentUser();
    List<User> getAllUsers();
    User updateProfile(Long id, User update);
    User createUser(User user);
}
