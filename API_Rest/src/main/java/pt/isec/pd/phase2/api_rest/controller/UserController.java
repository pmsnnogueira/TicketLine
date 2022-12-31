package pt.isec.pd.phase2.api_rest.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pd.phase2.api_rest.security.TokenService;
import pt.isec.pd.phase2.api_rest.service.UserService;

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
}
