package src;

public class Trip {
    private double tripDuration;

    public Trip() {
        this.tripDuration = 0.0;
    }

    public double getTripDuration() {
        return tripDuration;
    }

    public void setTripDuration(double tripDuration) {
        this.tripDuration = tripDuration;
    }

    public double computeTripDuration(TrainConnection tc) {
        String departureTime = tc.getDepartureTime();
        String arrivalTime = tc.getArrivalTime();

        arrivalTime = arrivalTime.replaceAll("\\s*\\(\\+\\d+d\\)", "");

        String[] depParts = departureTime.split(":");
        String[] arrParts = arrivalTime.split(":");

        int depHours = Integer.parseInt(depParts[0]);
        int depMinutes = Integer.parseInt(depParts[1]);
        int arrHours = Integer.parseInt(arrParts[0]);
        int arrMinutes = Integer.parseInt(arrParts[1]);

        if (arrHours < depHours || (arrHours == depHours && arrMinutes < depMinutes)) {
            arrHours += 24;
        }

        int totalDepMinutes = depHours * 60 + depMinutes;
        int totalArrMinutes = arrHours * 60 + arrMinutes;

        int durationInMinutes = totalArrMinutes - totalDepMinutes;

        this.tripDuration = durationInMinutes / 60.0;
        return this.tripDuration;
    }
}
