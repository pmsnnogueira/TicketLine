package pt.isec.pd.phase2.api_rest.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
        List<User> users = userService.getAllUsers();
        if(users == null)
            return ResponseEntity.badRequest().header("AdminAuthentication", "Please Authenticate as admin")
                                                .body(new ArrayList<>());
        else
            return ResponseEntity.ok().body(users);
    }
}
