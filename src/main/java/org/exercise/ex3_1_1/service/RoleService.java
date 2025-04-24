package org.exercise.ex3_1_1.service;

import org.exercise.ex3_1_1.model.Role;

import java.util.List;

public interface RoleService {
    List<Role> findAll();

    Role findById(Long id);
}
