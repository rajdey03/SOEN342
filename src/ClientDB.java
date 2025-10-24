package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

    public Client createClient() {
        Client client = new Client();
        clients.add(client);
        return client;
    }

     // finds the index of a client with the last name and id. returns -1 if not found
    public int findClient(String lastName, long id) {
        if (lastName == null) return -1;
        for (int i = 0; i < clients.size(); i++) {
            Client c = clients.get(i);
            if (c.getLastName() != null && lastName.equalsIgnoreCase(c.getLastName().trim()) && c.getClientId() == id) {
                return i;
            }
        }
        return -1;
    }

   //saves clients to a csv file
    public void saveClientsToFile(String path) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            // header
            bw.write("clientId,firstName,lastName,age");
            bw.newLine();
            for (Client c : clients) {
                String line = String.format("%d,%s,%s,%d",
                        c.getClientId(),
                        csvEscape(c.getFirstName()),
                        csvEscape(c.getLastName()),
                        c.getAge()
                );
                bw.write(line);
                bw.newLine();
            }
        }
    }

    //loads clients from a csv file, used in SystemDriver to load saved clients
    public void loadClientsFromFile(String path) throws IOException {
        clients.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = parseCSV(line);
                if (parts.length >= 4) {
                    Client c = new Client();
                    c.setClientId(Long.parseLong(parts[0]));
                    c.setFirstName(parts[1]);
                    c.setLastName(parts[2]);
                    c.setAge(Integer.parseInt(parts[3]));
                    clients.add(c);
                }
            }
        }
    }

    private String[] parseCSV(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (char ch : line.toCharArray()) {
            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                fields.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }
        fields.add(sb.toString().trim());
        return fields.toArray(new String[0]);
    }


    private String csvEscape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    //adding a couple of clients and saving to file demo
   public static void main(String[] args){
        ClientDB db = new ClientDB();
        Client a = new Client();
        a.setClientId(1);
        a.setFirstName("Alice");
        a.setLastName("Smith");
        a.setAge(30);
        db.getClients().add(a);

        Client b = new Client();
        b.setClientId(2);
        b.setFirstName("Bob");
        b.setLastName("Jones");
        b.setAge(40);
        db.getClients().add(b);

        String path = "clients.txt";
        try {
            db.saveClientsToFile(path);
            System.out.println("Saved " + db.getClients().size() + " clients to " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
