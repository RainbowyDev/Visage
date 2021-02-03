package net.square.utilities.location;

import net.square.utilities.lists.EvictingList;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.abs;

public class PastLocations {

    public final List<CustomLocation> previousLocations = new EvictingList<>(20);

    public List<CustomLocation> getEstimatedLocation(long time, long delta) {
        List<CustomLocation> toSort = new ArrayList<>(this.previousLocations);
        toSort.sort(Comparator.comparingLong(
            location -> abs(location.getTimeStamp() - (System.currentTimeMillis() - time))
        ));
        List<CustomLocation> list = new ArrayList<>();
        for (CustomLocation location : toSort) {
            if (abs(location.getTimeStamp() - (System.currentTimeMillis() - time)) < delta) {
                list.add(location);
            }
        }
        return list;
    }

    public void addLocation(Location location) {
        this.previousLocations.add(new CustomLocation(location));
    }
}