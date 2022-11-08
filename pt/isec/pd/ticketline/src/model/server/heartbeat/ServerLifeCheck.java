package pt.isec.pd.ticketline.src.model.server.heartbeat;

import pt.isec.pd.ticketline.src.model.ModelManager;
import pt.isec.pd.ticketline.src.ui.UI;

import java.util.function.Consumer;

public class ServerLifeCheck implements Runnable{
    private UI ui;

    public ServerLifeCheck(UI ui){
        this.ui = ui;
    }

    @Override
    public void run() {
        this.ui.serverLifeCheck();
    }
}
