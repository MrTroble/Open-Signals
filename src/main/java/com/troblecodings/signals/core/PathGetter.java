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
            path = Paths.get(server.getFolderName() + "/osfiles/" + subDirectory + "/"
                    + ((WorldServer) world).provider.getDimensionType().getName().replace(":", ""));
        } else if (!world.isRemote && !server.isDedicatedServer()) {
            path = Paths.get("saves/" + server.getFolderName() + "/osfiles/" + subDirectory + "/"
                    + ((WorldServer) world).provider.getDimensionType().getName().replace(":", ""));
        }
        return path;
    }

}
