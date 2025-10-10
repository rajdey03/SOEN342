package src;

import java.util.ArrayList;
import java.util.List;

public class TrainDB {
    private List<Train> trains;

    public TrainDB() {
        this.trains = new ArrayList<>();
    }

    public void addTrain(Train train) {
        trains.add(train);
    }

    public void removeTrain(Train train) {
        trains.remove(train);
    }

    public List<Train> getTrains() {
        return trains;
    }


}
