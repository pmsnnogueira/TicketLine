package pt.isec.pd.ticketline.src;

import pt.isec.pd.ticketline.src.model.Data;
import pt.isec.pd.ticketline.src.ui.UI;

public class Main {
    public static void main(String[] args){
        Data data = new Data();
        UI ui = new UI(data);

        System.out.println(ui);
    }
}
