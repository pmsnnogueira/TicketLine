package pt.isec.pd.phase2.api_rest.controller;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd.phase2.api_rest.model.Show;
import pt.isec.pd.phase2.api_rest.service.ShowService;

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

    /**
     * get all Shows
     * Postman : localhost:8080/show/all
     * @return List of shows
     */
    @GetMapping("/all")
    public ResponseEntity<List<Show>> getAllShows()
    {
        return ResponseEntity.ok().body(showService.getAllShows());
    }

    /**
     * localhost:8080/show
     * and send it a json file
     *{
     "id": 1,
     "designation": "teste1",
     "type": "testetype",
     "date": "testeDate",
     "hour": "testeHour",
     "duration": 45,
     "place": null,
     "city": "testeCity",
     "country": "testeCountry",
     "age": "testeAge",
     "visible": 0
     }
     * @param c
     * @return
     */
    @PostMapping
    public ResponseEntity<Show> createShow(@RequestBody Show c)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.createShow(c));
    }


    /**
     * get filtered shows
     * Postman ex: localhost:8080/show/id?value=1
     * @param filter
     * @param value
     * @return
     */
    @GetMapping("{filter}")
    public ResponseEntity<List<Show>> getFilteredShows(@PathVariable("filter") String filter,
                                                       @RequestParam(value = "value", required = true) String value)
    {
        switch (filter.toLowerCase()){
            case "id": return ResponseEntity.ok().body(showService.getShowsById(value));
            case "date" : return ResponseEntity.ok().body(showService.getShowsByDate(value));
            default: return ResponseEntity.badRequest().build();
        }
    }


}
