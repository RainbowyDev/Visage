package net.square.utilities.location;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class CustomLocation {

    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final long timeStamp;

    public CustomLocation(double x, double y, double z, float yaw, float pitch, long timeStamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.timeStamp = timeStamp;
    }

    public CustomLocation(Location loc) {
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
        this.timeStamp = System.currentTimeMillis();
    }

    public CustomLocation clone() {
        return new CustomLocation(this.x, this.y, this.z, this.yaw, this.pitch, this.timeStamp);
    }

    public Vector toVector() {
        return new Vector(this.x, this.y, this.z);
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }
}