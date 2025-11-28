package src;

public class DurationCalculator {

    public double computeAllTripDurations(TrainConnection tc) {
        return computeSingleDuration(tc.getDepartureTime(), tc.getArrivalTime());
    }

    private double computeSingleDuration(String dep, String arr) {
        String cleanDep = dep.split(" ")[0];
        String cleanArr = arr.split(" ")[0];

        String[] d = cleanDep.split(":");
        String[] a = cleanArr.split(":");

        int depH = Integer.parseInt(d[0]);
        int depM = Integer.parseInt(d[1]);
        int arrH = Integer.parseInt(a[0]);
        int arrM = Integer.parseInt(a[1]);

        // handle overnight arrival
        if (arrH < depH || (arrH == depH && arrM < depM)) {
            arrH += 24;
        }

        int startMin = depH * 60 + depM;
        int endMin = arrH * 60 + arrM;

        return (endMin - startMin) / 60.0;
    }
}
