package pt.isec.pd.phase2.api_rest.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd.phase2.api_rest.model.Show;
import pt.isec.pd.phase2.api_rest.service.ShowService;

import java.util.List;

@RestController
public class ShowController
{
    private final ShowService showService;

    @Autowired
    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    @GetMapping("/show")
    public ResponseEntity<List<Show>> getFilteredShows(@RequestParam(value = "date", required = true) String date)
    {
        return ResponseEntity.ok().body(showService.getShowsByDate(date));
    }
}
