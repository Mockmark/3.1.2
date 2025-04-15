package org.exercise.ex3_1_1.controller;

import org.exercise.ex3_1_1.model.Role;
import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.service.RoleService;
import org.exercise.ex3_1_1.service.ServiceProv;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    ServiceProv userService;
    RoleService roleService;
    PasswordEncoder passwordEncoder;

    UserController(ServiceProv userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(value = "")
    public String redirectToIndex() {
        return "redirect:/admin/index";
    }

    @GetMapping(value = "/index")
    public String index(ModelMap model) {
        List<User> users = userService.index();
        model.addAttribute("users", users);
        return "index";
    }

    @PostMapping(value = "/index")
    public String saveUser(@ModelAttribute("user") User user, ModelMap model) {
        Set<Role> fullRoles = user.getRoles().stream()
                .map(role -> roleService.findById(role.getId()))
                .collect(Collectors.toSet());

        user.setRoles(fullRoles);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("errorMessage",
                    "Username already exists.");
            model.addAttribute("user", user);
            model.addAttribute("allRoles", roleService.findAll());
            return "add";
        }

        userService.addUser(user);
        return "redirect:/admin/index";
    }

    @GetMapping(value = "/add")
    public String newUser(@ModelAttribute("user") User user, ModelMap model) {
        model.addAttribute("allRoles", roleService.findAll());
        return "add";
    }

    @GetMapping(value = "/edit")
    public String editUser(@RequestParam(name = "id") int id, ModelMap model) {
        User userToEdit = userService.getUserById(id);
        model.addAttribute("allRoles", roleService.findAll());
        model.addAttribute("userToEdit", userToEdit);
        return "edit";
    }

    @PostMapping(value = "/edit")
    public String updateUser(@ModelAttribute("userToEdit") User user, ModelMap model) {
        Set<Role> fullRoles = user.getRoles().stream()
                .map(role -> roleService.findById(role.getId()))
                .collect(Collectors.toSet());
        user.setRoles(fullRoles);
        User existingUser = userService.getUserById(user.getId());

        if (!user.getPassword().equals(existingUser.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        Optional<User> userWithSameUsername = userService.findByUsername(user.getUsername());

        if (userWithSameUsername.isPresent() && !(userWithSameUsername.get().getId() == (user.getId()))) {
            model.addAttribute("errorMessage", "Username already exists.");
            model.addAttribute("user", user);
            model.addAttribute("allRoles", roleService.findAll());
            return "edit";
        }

        userService.updateUser(user);
        return "redirect:/admin/index";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") int id) {
        userService.deleteUser(id);
        return "redirect:/admin/index";
    }
}
