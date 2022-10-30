package pt.isec.pd.ticketline.src;

import pt.isec.pd.ticketline.src.model.ModelManager;
import pt.isec.pd.ticketline.src.resources.files.FileOpener;
import pt.isec.pd.ticketline.src.ui.UI;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args){
        System.out.println(FileOpener.openFile("pt/isec/pd/ticketline/src/resources/files/teste.txt"));
        /*ModelManager modelManager;
        try{
            modelManager = new ModelManager();
        }catch (SQLException e){
            e.printStackTrace();
            return;
        }
        UI ui = new UI(modelManager);
        ui.start();*/
    }
}
