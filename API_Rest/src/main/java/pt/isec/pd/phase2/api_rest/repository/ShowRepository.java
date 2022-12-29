package pt.isec.pd.phase2.api_rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.isec.pd.phase2.api_rest.model.Show;

import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Integer>
{
    @Query("SELECT c FROM Show c")
    List<Show> listShows();

    List<Show> findById(String id);

}
