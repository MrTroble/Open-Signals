package com.troblecodings.signals.core;

import java.nio.file.Path;
import java.nio.file.Paths;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public final class PathGetter {

    private PathGetter() {

    }

    public static Path getNewPathForFiles(final World world, final String subDirectory) {
        final MinecraftServer server = world.getMinecraftServer();
        Path path = Paths.get("osfiles");
        if (!world.isRemote && server != null && server.isDedicatedServer()) {
            path = Paths.get(world.getMinecraftServer().getName().replace("/", "_").replace(".",
                    "_") + "/osfiles/" + subDirectory + "/"
                    + ((WorldServer) world).provider.getDimensionType().getName().replace(":", ""));
        } else if (!world.isRemote && (server == null || !server.isDedicatedServer())) {
            path = Paths.get("saves/"
                    + world.getMinecraftServer().getName().replace("/", "_").replace(".", "_")
                    + "/osfiles/" + subDirectory + "/"
                    + ((WorldServer) world).provider.getDimensionType().getName().replace(":", ""));
        }
        return path;
    }

}
