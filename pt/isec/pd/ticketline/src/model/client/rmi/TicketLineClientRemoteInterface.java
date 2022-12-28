package pt.isec.pd.ticketline.src.model.client.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TicketLineClientRemoteInterface extends Remote {
    void listActiveServers(String list) throws RemoteException;
}
