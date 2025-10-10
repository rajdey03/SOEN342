package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrainConnectionDB {
    private List<TrainConnection> trainConnections;

    public TrainConnectionDB(){
        trainConnections = new ArrayList<>();
    }

    public void loadCSV(String filePath) throws IOException{
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))){
            br.readLine();
            String line;
            while ((line = br.readLine()) != null){
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                System.out.println(fields[0]);
                System.out.println(fields[1]);
                System.out.println(fields[2]);
                System.out.println(fields[3]);
                System.out.println(fields[4]);
                System.out.println(fields[5]);
                System.out.println(fields[6]);
                System.out.println(fields[7]);
                System.out.println(fields[8]);

                TrainConnection tc = new TrainConnection();
                tc.setRouteID(fields[0]);
                tc.setDepartureCity(fields[1]);
                tc.setArrivalCity(fields[2]);
                tc.setDepartureTime(fields[3]);
                tc.setArrivalTime(fields[4]);
                tc.setTrain(new Train(fields[5]));
                tc.setDaysOfOperation(fields[6]);
                tc.setFirstClassRate(Double.parseDouble(fields[7]));
                tc.setSecondClassRate(Double.parseDouble(fields[8]));
                trainConnections.add(tc);
            }

        }
    }

    public List<TrainConnection> getTrainConnections() {
        return trainConnections;
    }

    public void addTrainConnection(TrainConnection trainConnection) {
        trainConnections.add(trainConnection);
    }

    public void removeTrainConnection(TrainConnection trainConnection) {
        trainConnections.remove(trainConnection);
    }




}
