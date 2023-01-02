package pt.isec.pd.phase2.api_rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.isec.pd.phase2.api_rest.model.Show;

import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Integer>
{
    @Query("SELECT s FROM Show s WHERE s.date BETWEEN :dateBeggining AND :dateEnd")
    List<Show> findShowBetweenDates(@Param("dateBeggining") String dateBeggining, @Param("dateEnd") String dateEnd);

    @Query("SELECT s FROM Show s WHERE s.date > :dateBeggining")
    List<Show> findShowBegginingIn(@Param("dateBeggining") String dateBeggining);

    @Query("SELECT s FROM Show s WHERE s.date < :dateEnd")
    List<Show> findShowBegginingBefore(@Param("dateEnd") String dateEnd);
}
