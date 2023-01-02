package pt.isec.pd.phase2.api_rest.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd.phase2.api_rest.model.User;
import pt.isec.pd.phase2.api_rest.security.TokenService;
import pt.isec.pd.phase2.api_rest.service.UserService;

import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    @PostMapping("/login")
    public String login(Authentication authentication)
    {
        return userService.tokenService.generateToken(authentication);
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAllUsers()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("SCOPE_Admin"));

        if(!isAdmin)
            return ResponseEntity.badRequest().header("AdminAuthentication", "You need to be an admin to perform that operation")
                    .body(new ArrayList<>());

        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok().body(users);
    }

    @PostMapping("/admin/register")
    public ResponseEntity<User> registerUser(@RequestBody User user)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("SCOPE_Admin"));

        if(!isAdmin)
            return ResponseEntity.badRequest().header("RegisterUser", "You need to be an admin to perform that operation")
                    .body(new User());

        if(user == null)
            return ResponseEntity.badRequest().header("RegisterUser", "You need to insert all user information.").
                    body(new User());

        User userCheck = userService.registerUser(user);

        if(userCheck == null)
            return ResponseEntity.badRequest().header("RegisterUser", "User with that username/name already exists").
                                                body(new User());
        else
            return ResponseEntity.ok().body(userCheck);
    }

    @DeleteMapping("/admin/delete")
    public ResponseEntity<User> deleteUser(@RequestParam String id)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("SCOPE_Admin"));

        if(!isAdmin)
            return ResponseEntity.badRequest().header("AdminAuthentication", "You need to be an admin to perform that operation")
                    .body(new User());

        User user;
        try {
            user = userService.deleteUser(Integer.parseInt(id));
            if(user == null){
                return ResponseEntity.badRequest().header("DeleteUser", "User with that Id doesnt exist.")
                        .body(new User());
            }
            else {
                return ResponseEntity.ok().body(user);
            }
        }
        catch (NumberFormatException nfe){
            return ResponseEntity.badRequest().header("DeleteUser", "Thats not a valid id for the user.").body(new User());
        }
    }
}
