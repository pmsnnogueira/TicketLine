package pt.isec.pd.phase2.api_rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.isec.pd.phase2.api_rest.model.User;
import pt.isec.pd.phase2.api_rest.repository.UserRepository;
import pt.isec.pd.phase2.api_rest.security.TokenService;

import java.util.List;

@Service
public class UserService
{
    public final TokenService tokenService;
    private final UserRepository userRepository;

    @Autowired
    public UserService(TokenService tokenService, UserRepository userRepository)
    {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    public List<User> getUser(String username, String password)
    {
        return userRepository.findUser(username, password);
    }

    public User getUserByName(String username)
    {
        return userRepository.findByName(username);
    }
}
