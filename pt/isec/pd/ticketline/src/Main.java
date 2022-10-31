package pt.isec.pd.ticketline.src;

import pt.isec.pd.ticketline.src.model.ModelManager;
import pt.isec.pd.ticketline.src.ui.UI;
import java.sql.SQLException;
public class Main {
    public static void main(String[] args){
        
        ModelManager modelManager;
        try{
            modelManager = new ModelManager();
        }catch (SQLException e){
            e.printStackTrace();
            return;
        }
        UI ui = new UI(modelManager);
        ui.start();
    }
}
