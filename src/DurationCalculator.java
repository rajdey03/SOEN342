package src;

public class DurationCalculator {

    public double computeAllTripDurations(TrainConnection tc) {
        Trip trip = new Trip();
        return trip.computeTripDuration(tc);
    }
}
