package pt.isec.pd.ticketline.src.ui;

import pt.isec.pd.ticketline.src.model.client.rmi.ClientRMI;
import pt.isec.pd.ticketline.src.ui.util.InputProtection;

public class ClientRMI_UI {
    private final ClientRMI client;

    public String art(){
        return ("\n" +
                "  _____ ___ ____ _  _______ _____ _     ___ _   _ _____    ____ _     ___ _____ _   _ _____     ____  __  __ ___ \n" +
                " |_   _|_ _/ ___| |/ / ____|_   _| |   |_ _| \\ | | ____|  / ___| |   |_ _| ____| \\ | |_   _|   |  _ \\|  \\/  |_ _|\n" +
                "   | |  | | |   | ' /|  _|   | | | |    | ||  \\| |  _|   | |   | |    | ||  _| |  \\| | | |_____| |_) | |\\/| || | \n" +
                "   | |  | | |___| . \\| |___  | | | |___ | || |\\  | |___  | |___| |___ | || |___| |\\  | | |_____|  _ <| |  | || | \n" +
                "   |_| |___\\____|_|\\_\\_____| |_| |_____|___|_| \\_|_____|  \\____|_____|___|_____|_| \\_| |_|     |_| \\_\\_|  |_|___|\n" +
                "                                                                                                                 \n");
    }

    public ClientRMI_UI(ClientRMI client){
        this.client = client;
    }

    public void registerCallback() {
        int input = InputProtection.chooseOption("What callback do you want to register",
                "TCP listener", "UDP listener", "Login listener", "TCP disconnect listener");

        switch (input){
            case 1 -> {
                if(!client.addListener("TCP")){
                    System.out.println("Callback registration unsuccessful");
                    return;
                }
                System.out.println("Callback registration successful");
            }
            case 2 -> {
                if(!client.addListener("UDP")){
                    System.out.println("Callback registration unsuccessful");
                    return;
                }
                System.out.println("Callback registration successful");
            }
            case 3 -> {
                if(!client.addListener("LOGIN")){
                    System.out.println("Callback registration unsuccessful");
                    return;
                }
                System.out.println("Callback registration successful");
            }
            case 4 -> {
                if(!client.addListener("LOST")){
                    System.out.println("Callback registration unsuccessful");
                    return;
                }
                System.out.println("Callback registration successful");
            }
        }
    }

    private void removeCallback() {
        int input = InputProtection.chooseOption("What callback do you want to remove",
                "TCP listener", "UDP listener", "Login listener", "TCP disconnect listener");

        switch (input){
            case 1 -> {
                if(client.removeListener("TCP")){
                    System.out.println("Callback registration unsuccessful");
                    return;
                }
                System.out.println("Callback registration successful");
            }
            case 2 -> {
                if(client.removeListener("UDP")){
                    System.out.println("Callback registration unsuccessful");
                    return;
                }
                System.out.println("Callback registration successful");
            }
            case 3 -> {
                if(client.removeListener("LOGIN")){
                    System.out.println("Callback registration unsuccessful");
                    return;
                }
                System.out.println("Callback registration successful");
            }
            case 4 -> {
                if(client.removeListener("LOST")){
                    System.out.println("Callback registration unsuccessful");
                    return;
                }
                System.out.println("Callback registration successful");
            }
        }
    }

    public void listActiveServers(){
        client.list();
    }

    public void start(){
        System.out.println(art());
        while(true){
            int input = InputProtection.chooseOption("Choose an action:",
                    "List active servers", "Register callback for UDP connections",
                    "Remove callback for TCP connections" , "Exit");

            switch (input){
                case 1 ->listActiveServers();
                case 2 -> registerCallback();
                case 3 -> removeCallback();
                case 4 -> {
                    return;
                }
            }
        }
    }
}
