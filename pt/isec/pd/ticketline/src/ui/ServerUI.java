package pt.isec.pd.ticketline.src.ui;

import pt.isec.pd.ticketline.src.model.ModelManager;
import pt.isec.pd.ticketline.src.model.server.Server;
import pt.isec.pd.ticketline.src.model.server.heartbeat.HeartBeat;
import pt.isec.pd.ticketline.src.ui.util.InputProtection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ServerUI {
    private final ModelManager data;

    public ServerUI(ModelManager data){
        this.data = data;
    }

    public String artServer (){
        return ("""
                                        
                  _______ _____ _____ _  ________ _______ _      _____ _   _ ______    _____ ______ _______      ________ _____ \s
                 |__   __|_   _/ ____| |/ /  ____|__   __| |    |_   _| \\ | |  ____|  / ____|  ____|  __ \\ \\    / /  ____|  __ \\\s
                    | |    | || |    | ' /| |__     | |  | |      | | |  \\| | |__    | (___ | |__  | |__) \\ \\  / /| |__  | |__) |
                    | |    | || |    |  < |  __|    | |  | |      | | | . ` |  __|    \\___ \\|  __| |  _  / \\ \\/ / |  __| |  _  /\s
                    | |   _| || |____| . \\| |____   | |  | |____ _| |_| |\\  | |____   ____) | |____| | \\ \\  \\  /  | |____| | \\ \\\s
                    |_|  |_____\\_____|_|\\_\\______|  |_|  |______|_____|_| \\_|______| |_____/|______|_|  \\_\\  \\/   |______|_|  \\_\\""");
    }

    private void listAllAvailableServers() {
        System.out.println(this.data.listAllAvailableServers());
    }

    public void start(){
        System.out.println(artServer());
        while (true){
            try{
                Thread.sleep(500);
            }catch (InterruptedException ignored){
            }

            int input = InputProtection.chooseOption("Choose an action:", "List available servers","Exit");

            switch (input){
                /*case 1 -> listInformation();
                case 1 -> listInformation();
                case 2 -> insertData();
                case 3 -> deleteData();
                case 4 -> updateData();*/
                case 1 -> listAllAvailableServers();
                case 2 -> {
                    try{
                        this.data.closeServer();
                    }catch (SQLException | IOException | InterruptedException ignored){}
                    return;
                }
            }
        }
    }

}
