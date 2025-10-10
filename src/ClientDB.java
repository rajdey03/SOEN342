package src;

import java.util.ArrayList;
import java.util.List;

public class ClientDB {
    private List<Client> clients;

    public ClientDB() {
        clients = new ArrayList<>();
    }

    public List<Client> getClients() {
        return clients;
    }

    public void addClient(Client client) {
        clients.add(client);
    }
}
