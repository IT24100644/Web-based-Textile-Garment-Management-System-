package com.lankatex.garmentms.web;

import com.lankatex.garmentms.dto.CreateUserRequest;
import com.lankatex.garmentms.model.Role;
import com.lankatex.garmentms.model.User;
import com.lankatex.garmentms.model.enums.RoleName;
import com.lankatex.garmentms.repository.RoleRepository;
import com.lankatex.garmentms.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(UserRepository users,
                               RoleRepository roles,
                               PasswordEncoder passwordEncoder) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping({"/list", ""})
    public String list(@RequestParam(name = "page", defaultValue = "0") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       @RequestParam(name = "msg", defaultValue = "") String msg,
                       Model model) {

        Page<User> p = users.findAll(
                PageRequest.of(page, size, Sort.by("id").descending())
        );

        model.addAttribute("page", p);                  // <-- fixes ${page.content}
        model.addAttribute("roles", RoleName.values()); // <-- drives <option th:each="r : ${roles}">
        model.addAttribute("msg", msg);  // <-- Display messages from redirection
        return "admin/users/list";
    }

    @PostMapping({"/{id}/deactivate", "/{id}/deactivate/"})
    public String deactivate(@PathVariable("id") Long id, RedirectAttributes ra) {
        return users.findById(id).map(u -> {
            u.setActive(false);
            users.save(u);
            ra.addFlashAttribute("msg", "User deactivated");
            return "redirect:/admin/users/list";
        }).orElseGet(() -> {
            ra.addFlashAttribute("err", "User not found");
            return "redirect:/admin/users/list";
        });
    }

    @PostMapping({"/{id}/activate", "/{id}/activate/"})
    public String activate(@PathVariable("id") Long id, RedirectAttributes ra) {
        return users.findById(id).map(u -> {
            u.setActive(true);
            users.save(u);
            ra.addFlashAttribute("msg", "User Activated");
            return "redirect:/admin/users/list";
        }).orElseGet(() -> {
            ra.addFlashAttribute("err", "User not found");
            return "redirect:/admin/users/list";
        });
    }

    @PostMapping({"/{id}/delete", "/{id}/delete/"})
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        if (!users.existsById(id)) {
            ra.addFlashAttribute("err", "User not found");
        } else {
            users.deleteById(id);
            ra.addFlashAttribute("msg", "User deleted");
        }
        return "redirect:/admin/users/list";
    }



    @PostMapping("/{id}/assign-role")
    public String assignRole(@PathVariable("id") Long id,
                             @RequestParam("role") RoleName roleName,
                             RedirectAttributes ra) {
        User u = users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Role newRole = roles.findByName(roleName).orElseThrow(() -> new IllegalArgumentException("Role not found"));

        u.getRoles().clear();        // <-- replace instead of append
        u.getRoles().add(newRole);
        users.saveAndFlush(u);       // ensure join table is updated before redirect

        ra.addFlashAttribute("msg", "Role updated to " + roleName);
        return "redirect:/admin/users/list";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable("id") Long id,
                                @RequestParam("newPassword") String newPassword) {
        User u = users.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        u.setPasswordHash(passwordEncoder.encode(newPassword));
        users.save(u);
        return "redirect:/admin/users/list?msg=Password%20reset";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new CreateUserRequest()); // your DTO
        }
        model.addAttribute("roles", RoleName.values());
        return "admin/users/create"; // -> templates/admin/users/create.html
    }

    @PostMapping({"", "/"})
    public String createUser(@ModelAttribute("form") CreateUserRequest form,
                             Model model) {

        // Optional: validate username uniqueness
        if (users.existsByUsername(form.getUsername())) {
            model.addAttribute("roles", RoleName.values());
            model.addAttribute("usernameError", "Username already exists");
            return "admin/users/create";
        }

        User u = new User();
        u.setUsername(form.getUsername());
        u.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        u.setActive(true);

        // Single role selection from the form
        Role role = roles.findByName(form.getRole()).orElseThrow(() -> new IllegalArgumentException("Role not found"));
        u.getRoles().add(role);

        users.save(u);
        return "redirect:/admin/users/list?msg=User%20created";
    }
}
