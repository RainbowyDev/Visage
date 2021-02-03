package net.square.storage;

import net.square.utilities.lists.EvictingList;
import net.square.utilities.location.PastLocations;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DataStorage {

    public EvictingList<Float> yawAccelSamples = new EvictingList<>(20);
    public EvictingList<Float> pitchAccelSamples = new EvictingList<>(20);
    public EvictingList<Double> differenceSamples = new EvictingList<>(25);

    public boolean inCombat = false;
    public boolean swungArm = false;

    public int totalMoves = 0;
    public int totalAimMoves = 0;
    public int totalAimPosLook = 0;
    public int swings = 0;
    public int hits = 0;

    public int reachThreshold = 0;
    public int swingThreshold = 0;
    public int combatAnalyticsThreshold = 0;
    public int attackRaytraceThreshold = 0;
    public int assistA1Threshold = 0;
    public int assistA2Threshold = 0;
    public int assistA4Threshold = 0;
    public int assistA6Threshold = 0;
    public int heuristicsA1Threshold = 0;
    public int heuristicsA3Threshold = 0;
    public int heuristicsA4Threshold = 0;
    public int heuristicsA5Threshold = 0;
    public int heuristicsA6Threshold = 0;
    public int auraA2Threshold = 0;
    public int auraA3Threshold = 0;

    public long ping = 0;
    public long lastServerKeepAlive = 0;
    public long lastFlying = 0;
    public long lastBowPull = 0;
    public long lastAttackTime = 0;

    public double lastPitchDelta = 0;
    public double lastYawDelta = 0;
    public double lastMovement = 0;
    public double yawAccel = 0;
    public double pitchAccel = 0;
    public double lastEntityHit = 0;

    public float lastPitch = 0;
    public float lastYaw = 0;
    public float lastAimPitch = 0;
    public float lastAuraYaw = 0;
    public float lastAuraPitch = 0;

    public Player player;
    public LivingEntity livingEntity;
    public LivingEntity lastCombatEntity;
    public Entity lastEntity;
    public Entity lastAuraEntity;

    public Location lastAttackLocation;

    public PastLocations entityPastLocations = new PastLocations();

    public List<Double> distances = new ArrayList<>();

    public long elapsed(long now, long start) { return now - start; }

    DataStorage(Player player) {
        this.player = player;
    }
}