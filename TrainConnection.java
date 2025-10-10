import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.time.chrono.JapaneseEra.values;

public class TrainConnection {
    private String routeID;
    private String departureCity;
    private String arrivalCity;
    private String departureTime;
    private String arrivalTime;
    private Train train;
    private List<String> daysOfOperation;
    private double firstClassRate;
    private double secondClassRate;


    public String getRouteID() {
        return routeID;
    }

    public void setRouteID(String routeID) {
        this.routeID = routeID;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(String departureCity) {
        this.departureCity = departureCity;
    }

    public String getArrivalCity() {
        return arrivalCity;
    }

    public void setArrivalCity(String arrivalCity) {
        this.arrivalCity = arrivalCity;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public List<String> getDaysOfOperation() {
        return daysOfOperation;
    }

    public void setDaysOfOperation(String field) {
        String[] weekDays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        ArrayList<String> days = new ArrayList<String>();

        if (field.contains("-")) {
            String[] temp_fields = field.split("-");
            String start = temp_fields[0];
            String end = temp_fields[1];
            int i = 0;
            while (i < weekDays.length && !weekDays[i].equals(start)) {
                i++;
            }
            while (i < weekDays.length) {
                days.add(weekDays[i]);
                if (weekDays[i].equals(end)) {
                    break;
                }
                i++;
            }
            this.daysOfOperation = days;
        }


        else if (field.contains("\"")){
            field = field.replace("\"", "");
            String[] temp_fields = field.split(",");

            days.addAll(Arrays.asList(temp_fields));
            this.daysOfOperation = days;
        }

        else if (field.equals("Daily")){
            days.addAll(Arrays.asList(weekDays));
            this.daysOfOperation = days;
        }

        else {
            System.out.println("Error parsing days of operation");
        }


    }

    public double getFirstClassRate() {
        return firstClassRate;
    }

    public void setFirstClassRate(double firstClassRate) {
        this.firstClassRate = firstClassRate;
    }

    public double getSecondClassRate() {
        return secondClassRate;
    }

    public void setSecondClassRate(double secondClassRate) {
        this.secondClassRate = secondClassRate;
    }
}
