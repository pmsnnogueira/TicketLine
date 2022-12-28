package pt.isec.pd.ticketline.src.ui;

import pt.isec.pd.ticketline.src.model.client.rmi.ClientRMI;
import pt.isec.pd.ticketline.src.ui.util.InputProtection;

public class ClientRMI_UI {
    private final ClientRMI client;

    public ClientRMI_UI(ClientRMI client){
        this.client = client;
    }


    public void listActiveServers(){
        client.list();
    }

    public void start(){
        System.out.println("ClientRMI");
        while(true){
            int input = InputProtection.chooseOption("Choose an action:",
                    "List active servers", "Register callback for UDP connections",
                    "Remove callback for TCP connections" , "Exit");

            switch (input){
                case 1 ->listActiveServers();
//                case 2 ->
//                case 3 ->
                case 4 -> {
                    return;
                }
            }
        }
    }
}
