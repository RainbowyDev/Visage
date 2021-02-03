package net.square.utilities.permission;

public enum PermissionHandler {

    COMMAND_GUI("visage.command.gui"),
    COMMAND_TPS("visage.command.tps"),
    COMMAND_ENABLED("visage.command.enabled"),
    COMMAND_VERBOSE("visage.command.verbose"),
    COMMAND_NOTIFY("visage.command.notify"),
    COMMAND_DEBUG("visage.command.debug"),
    COMMAND_KICKS("visage.command.kicks"),
    COMMAND_CHECK("visage.command.check"),
    COMMAND_BROADCAST("visage.command.broadcast"),
    COMMAND_LOGS("visage.command.logs"),
    
    ADMIN_BYPASS("visage.bypass"),
    ADMIN_NOTIFICATION("visage.notification");

    private final String bukkitPermission;

    PermissionHandler(String bukkitPermission) {
        this.bukkitPermission = bukkitPermission;
    }

    public final String getBukkitPermission() {
        return this.bukkitPermission;
    }

}
