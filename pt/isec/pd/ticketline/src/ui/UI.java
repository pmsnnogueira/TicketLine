package pt.isec.pd.ticketline.src.ui;

import pt.isec.pd.ticketline.src.model.Data;

public class UI {
    private Data data;

    public UI(Data data){
        this.data = data;
    }

    @Override
    public String toString() {
        return this.data.getString();
    }
}
