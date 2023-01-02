package pt.isec.pd.phase2.api_rest.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.isec.pd.phase2.api_rest.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>
{
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.password = :password")
    User authenticateUser(@Param("username") String username, @Param("password") String password);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    User findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.name = :name")
    User findByName(@Param("name") String name);
}
