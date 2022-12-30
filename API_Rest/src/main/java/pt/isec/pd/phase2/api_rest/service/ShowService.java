package pt.isec.pd.phase2.api_rest.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import pt.isec.pd.phase2.api_rest.model.Show;
import pt.isec.pd.phase2.api_rest.repository.ShowRepository;

import java.util.ArrayList;
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
        return showRepository.findAll();
    }


    public List<Show> getShowsBetweenDates(String dateBeggining, String dateEnd)
    {
        return showRepository.findShowBetweenDates(dateBeggining, dateEnd);
    }

    public List<Show> getShowsStartingIn(String dateBeggining)
    {
        return showRepository.findShowBegginingIn(dateBeggining);
    }

    public List<Show> getShowsBegginingBefore(String dateEnd)
    {
        return showRepository.findShowBegginingBefore(dateEnd);
    }

}
