package org.exercise.ex3_1_1.service;

import org.exercise.ex3_1_1.model.Role;
import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.repository.DataAccess;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements ServiceProv {
    private final DataAccess userDao;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserService(DataAccess userDao, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Override
    @Transactional
    public void addUser(User user) {
        roleRehydration(user);
        passwordEnsure(user);
        userDao.save(user);
    }

    @Override
    @Transactional
    public void roleRehydration(User user) {
        Set<Role> fullRoles = user.getRoles().stream()
                .map(role -> roleService.findById(role.getId()))
                .collect(Collectors.toSet());
        user.setRoles(fullRoles);
    }

    @Override
    @Transactional
    public void passwordEnsure(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    @Override
    @Transactional
    public void passwordEnsureExtra(User user) {
        User existingUser = getUserById(user.getId());
        if (!user.getPassword().equals(existingUser.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> index() {
        return userDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(long id) {
        return userDao.findUserWithRolesById(id);
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        roleRehydration(user);
        passwordEnsureExtra(user);
        userDao.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(long id) {
        userDao.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userDao.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userDao.findByUsername(username);
    }


}
