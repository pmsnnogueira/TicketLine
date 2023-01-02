package pt.isec.pd.phase2.api_rest.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public User getUser(String username, String password)
    {
        return userRepository.authenticateUser(username, password);
    }

    public User getUserByUsername(String username)
    {
        return userRepository.findByUsername(username);
    }

    public User getUserByName(String name){return userRepository.findByName(name);}

    public List<User> getAllUsers()
    {
        return userRepository.findAll();
    }

    public User registerUser(User user)
    {
        User userCheck = getUserByUsername(user.getUsername());
        if(userCheck != null)
            return null;

        userCheck = getUserByName(user.getName());
        if(userCheck != null)
            return null;

        user.setId(null);
        return userRepository.save(user);
    }

    public User deleteUser(Integer id)
    {
        if(userRepository.findById(id).isPresent())
        {
            User currUser = userRepository.findById(id).get();
            userRepository.deleteById(id);
            return currUser;
        }
        else
        {
            return null;
        }
    }
}
