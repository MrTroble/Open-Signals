package com.troblecodings.signals.core;

import java.nio.file.Path;
import java.nio.file.Paths;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public final class PathGetter {

    public static Path getNewPathForFiles(final Level world, final String subDirectory) {
        final MinecraftServer server = world.getServer();
        Path path = Paths.get("osfiles");
        if (!world.isClientSide && server != null && server.isDedicatedServer()) {
            path = Paths.get(world.getServer().getWorldData().getLevelName().replace("/", "_")
                    .replace(".", "_") + "/osfiles/" + subDirectory + "/"
                    + world.dimension().location().toString().replace(":", ""));
        } else if (!world.isClientSide && (server == null || !server.isDedicatedServer())) {
            path = Paths.get("saves/"
                    + world.getServer().getWorldData().getLevelName().replace("/", "_").replace(".",
                            "_")
                    + "/osfiles/" + subDirectory + "/"
                    + world.dimension().location().toString().replace(":", ""));
        }
        return path;
    }
}