package pt.isec.pd.phase2.api_rest.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import pt.isec.pd.phase2.api_rest.model.User;
import pt.isec.pd.phase2.api_rest.repository.UserRepository;
import pt.isec.pd.phase2.api_rest.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserAuthenticationProvider implements AuthenticationProvider
{
    private final UserService userService;

    @Autowired
    public UserAuthenticationProvider(UserService userService)
    {
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        User user = userService.getUser(username, password);

        if(user == null)
            return null;
        else
        {
            List<GrantedAuthority> authorities = new ArrayList<>();
            if(user.getAdmin() == 0)
                authorities.add(new SimpleGrantedAuthority("User"));
            else
                authorities.add(new SimpleGrantedAuthority("Admin"));
            return new UsernamePasswordAuthenticationToken(username, password, authorities);
        }
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
