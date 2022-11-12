package pt.isec.pd.ticketline.src.model.server.heartbeat;

import pt.isec.pd.ticketline.src.model.ModelManager;
import pt.isec.pd.ticketline.src.model.data.Data;
import pt.isec.pd.ticketline.src.ui.ServerUI;

public class ServerLifeCheck implements Runnable{
    private Data data;

    public ServerLifeCheck(Data data){
        this.data = data;
    }

    @Override
    public void run() {
        this.data.serverLifeCheck();
    }
}
