package pt.isec.pd.ticketline.src.ui;

import pt.isec.pd.ticketline.src.model.ModelManager;
import pt.isec.pd.ticketline.src.ui.util.InputProtection;

import java.sql.SQLException;

public class UI {
    private final ModelManager data;

    public UI(ModelManager data){
        this.data = data;
    }

    private void listInformation(){
        int input = InputProtection.chooseOption(null, "List shows", "List reservations",
                                                 "List seats", "List users");
        switch (input){
            case 1 -> {
                int id = InputProtection.readInt("Show ID (-1 for all shows): ");
                try{
                    System.out.println(this.data.listShows(id == -1 ? null : id));
                }catch (SQLException e){
                    System.out.println("SQLException detected!");
                }
            }
            case 2 ->{
                int id = InputProtection.readInt("Reservation ID (-1 for all reservations): ");
                try{
                    System.out.println(this.data.listReservations(id == -1 ? null : id));
                }catch (SQLException e){
                    System.out.println("SQLException detected!");
                }
            }
            case 3 ->{
                int id = InputProtection.readInt("Seats ID (-1 for all seats): ");
                try{
                    System.out.println(this.data.listSeats(id == -1 ? null : id));
                }catch (SQLException e){
                    System.out.println("SQLException detected!");
                }
            }
            case 4 ->{
                int id = InputProtection.readInt("User ID (-1 for all users): ");
                try{
                    System.out.println(this.data.listUsers(id == -1 ? null : id));
                }catch (SQLException e){
                    System.out.println("SQLException detected!");
                }
            }
            default -> {
                System.out.println("Not a valid option! Try again!");
                listInformation();
            }
        }
    }

    public void start(){
        while (true){
            int input = InputProtection.chooseOption("Choose an action:", "List information",
                                                     "To be developed", "Exit");

            switch (input){
                case 1 -> listInformation();
                case 2 -> System.out.println("To be developed");
                case 3 -> {
                    System.out.println("HASTA LA VISTA BABY!");
                    return;
                }
            }
        }
    }
}
