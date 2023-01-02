package pt.isec.pd.phase2.api_rest.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd.phase2.api_rest.model.Show;
import pt.isec.pd.phase2.api_rest.service.ShowService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("show")
public class ShowController
{
    private final ShowService showService;

    @GetMapping("/")
    public String index(){
        return "Welcome to TicketLine API_Rest";
    }
    @Autowired
    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Show>> getFilteredShows(@RequestParam(required = false) String dateBeggining, @RequestParam(required = false) String dateEnd)
    {
        if(dateBeggining != null && dateEnd != null)
        {
            dateBeggining = dateBeggining.replace(" ", "-");
            dateEnd = dateEnd.replace(" ", "-");
            return ResponseEntity.ok().body(showService.getShowsBetweenDates(dateBeggining, dateEnd));
        }
        if(dateBeggining != null)
        {
            dateBeggining = dateBeggining.replace(" ", "-");
            return ResponseEntity.ok().body(showService.getShowsStartingIn(dateBeggining));
        }
        if(dateEnd != null)
        {
            dateEnd = dateEnd.replace(" ", "-");
            return ResponseEntity.ok().body(showService.getShowsBegginingBefore(dateEnd));
        }
        else
        {
            return ResponseEntity.ok().body(showService.getAllShows());
        }
    }

}
