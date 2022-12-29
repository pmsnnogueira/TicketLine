package pt.isec.pd.phase2.api_rest.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import pt.isec.pd.phase2.api_rest.model.Show;
import pt.isec.pd.phase2.api_rest.repository.ShowRepository;

import java.util.List;

@Service
public class ShowService
{
    private ShowRepository showRepository;
    private EntityManagerFactory emf;
    @Autowired
    public ShowService(ShowRepository showRepository)
    {
        this.showRepository = showRepository;
    }

    public List<Show> getAllShows() {
        return showRepository.listShows();
    }

    public List<Show> getShowsById(String id)
    {
        return showRepository.findById(id);
    }

    public List<Show> getShowsByDate(String date)
    {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Show> query = em.createQuery("select s from Show s where s.date = :date", Show.class);
        query.setParameter("date", date);
        return query.getResultList();
    }


    public Show createShow(Show c) {
        c.setId(null);
        return showRepository.save(c);
    }
}
