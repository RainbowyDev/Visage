package net.square.processor;

import cc.funkemunky.api.events.AtlasListener;
import cc.funkemunky.api.events.Listen;
import cc.funkemunky.api.events.impl.PacketReceiveEvent;
import cc.funkemunky.api.events.impl.PacketSendEvent;
import cc.funkemunky.api.utils.Init;
import io.netty.util.concurrent.FastThreadLocalThread;
import net.square.module.ModuleManager;
import net.square.module.VisageCheck;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Init
public class PacketProcessor implements AtlasListener {

    private final ModuleManager moduleManager;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public PacketProcessor(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    @Listen
    public void handle(PacketReceiveEvent event) {
        this.executorService.submit(() -> {
            Player player = event.getPlayer();
            for (VisageCheck module : this.modules()) {
                if (module.isTypeToLookFor(event)) {
                    module.handleIn(player, event);
                }
            }
        });
    }

    @Listen
    public void handle(PacketSendEvent event) {
        this.executorService.submit(() -> {
            Player player = event.getPlayer();
            for (VisageCheck module : this.modules()) {
                if (module.isTypeToLookFor(event)) {
                    module.handleOut(player, event);
                }
            }
        });
    }

    private Collection<VisageCheck> modules() {
        return this.moduleManager.modules.values();
    }

    public void shutdown() {
        this.executorService.shutdown();
    }
}