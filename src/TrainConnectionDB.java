package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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


    public List<TrainConnection> findConnections(String departureCity, String arrivalCity, Map<String,String> filters) {
        List<TrainConnection> results = new ArrayList<>();
        for (TrainConnection tc : trainConnections){
            if (tc.getDepartureCity().equalsIgnoreCase(departureCity) &&
                tc.getArrivalCity().equalsIgnoreCase(arrivalCity)){
                results.add(tc);
            }
        }

        for(Map.Entry<String, String> filter : filters.entrySet()){
            String key = filter.getKey();
            String value = filter.getValue();
            results.removeIf(tc -> {
                switch (key) {
                    case "departureDay":
                        return !tc.getDaysOfOperation().contains(value);
                    case "arrivalDay":
                        return !tc.getDaysOfOperation().contains(value);
                    case "trainType":
                        return !tc.getTrain().getTrainType().equalsIgnoreCase(value);
                    case "departureTime":
                        return tc.getDepartureTime().compareTo(value) < 0;
                    case "arrivalTime":
                        return tc.getArrivalTime().compareTo(value) > 0;
                    case "minFirstClassPrice":
                        return tc.getFirstClassRate() < Double.parseDouble(value);
                    case "maxFirstClassPrice":
                        return tc.getFirstClassRate() > Double.parseDouble(value);
                    case "minSecondClassPrice":
                        return tc.getSecondClassRate() < Double.parseDouble(value);
                    case "maxSecondClassPrice":
                        return tc.getSecondClassRate() > Double.parseDouble(value);
                    default:
                        return false;
                }
            });
        }

        return results;
    }

    public List<TrainConnection> findIndirectConnections(String departureCity, String arrivalCity) {
        List<TrainConnection> results = new ArrayList<>();

        // Algorithm to find 1-stop connections
        for (TrainConnection firstLeg : trainConnections) {
            if (firstLeg.getDepartureCity().equalsIgnoreCase(departureCity)) {
                for (TrainConnection secondLeg : trainConnections) {
                    if (secondLeg.getDepartureCity().equalsIgnoreCase(firstLeg.getArrivalCity()) &&
                            secondLeg.getArrivalCity().equalsIgnoreCase(arrivalCity)) {
                        results.add(firstLeg);
                        results.add(secondLeg);
                    }
                }
            }
        }

        // Algorithm to find 2-stop connections
        for (TrainConnection firstLeg : trainConnections) {
            if (firstLeg.getDepartureCity().equals(departureCity) && !firstLeg.getArrivalCity().equals(arrivalCity)) {
                for (TrainConnection secondLeg : trainConnections) {
                    if (secondLeg.getDepartureCity().equals(firstLeg.getArrivalCity()) && !secondLeg.getArrivalCity().equals(arrivalCity)
                            && !secondLeg.getArrivalCity().equals(departureCity) && !secondLeg.getDepartureCity().equals(departureCity)) {
                        for (TrainConnection thirdLeg : trainConnections) {
                            if (thirdLeg.getDepartureCity().equals(secondLeg.getArrivalCity()) && thirdLeg.getArrivalCity().equals(arrivalCity)
                                    && !thirdLeg.getDepartureCity().equals(departureCity) && !thirdLeg.getDepartureCity().equals(firstLeg.getArrivalCity())) {
                                results.add(firstLeg);
                                results.add(secondLeg);
                                results.add(thirdLeg);
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

}
