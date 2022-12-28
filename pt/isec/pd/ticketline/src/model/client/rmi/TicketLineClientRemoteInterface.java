package pt.isec.pd.ticketline.src.model.client.rmi;

import java.rmi.RemoteException;

public interface TicketLineClientRemoteInterface {
    void listActiveServers(String list) throws RemoteException;
}
