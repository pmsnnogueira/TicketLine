package pt.isec.pd.phase2.api_rest.controller;


import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pd.phase2.api_rest.security.TokenService;

@RestController
public class UserController {

    private final TokenService tokenService;

    public UserController(TokenService tokenService)
    {
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public String login(Authentication authentication)
    {
        return tokenService.generateToken(authentication);
    }
}
