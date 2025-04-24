package org.exercise.ex3_1_1.controller;

import org.exercise.ex3_1_1.model.User;
import org.exercise.ex3_1_1.service.RoleService;
import org.exercise.ex3_1_1.service.ServiceProv;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    private final ServiceProv userService;
    private final RoleService roleService;

    UserController(ServiceProv userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping(value = "")
    public String redirectToIndex() {
        return "redirect:/admin/index";
    }

    @GetMapping(value = "/index")
    public String index(@AuthenticationPrincipal User userHomeAdm, ModelMap model) {
        model.addAttribute("userHomeAdm", userHomeAdm);

        List<User> users = userService.index();
        model.addAttribute("users", users);
        return "index";
    }

    @PostMapping(value = "/add")
    public String saveUser(@ModelAttribute("user") User user, ModelMap model) {
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("errorMessage",
                    "Username already exists.");
            model.addAttribute("user", user);
            model.addAttribute("allRoles", roleService.findAll());
            return "add";
        }
        userService.passwordEnsure(user);
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
        User existingUser = userService.getUserById(user.getId());
        userService.passwordEnsure(user, existingUser);

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
