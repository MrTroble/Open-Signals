package com.troblecodings.signals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import com.google.common.io.ByteStreams;
import com.troblecodings.contentpacklib.FileReader;
import com.troblecodings.core.net.NetworkHandler;
import com.troblecodings.guilib.ecs.GuiHandler;
import com.troblecodings.signals.handler.NameHandler;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.init.OSSounds;
import com.troblecodings.signals.proxy.CommonProxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = OpenSignalsMain.MODID,acceptedMinecraftVersions = "[1.12.2]")
public class OpenSignalsMain {

    @Instance
    private static OpenSignalsMain instance;
    public static final String MODID = "opensignals";

    public static OpenSignalsMain getInstance() {
        return instance;
    }

    public OpenSignalsMain() {
        instance = this;
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(OSBlocks.class);
        MinecraftForge.EVENT_BUS.register(OSItems.class);
        MinecraftForge.EVENT_BUS.register(OSSounds.class);
        MinecraftForge.EVENT_BUS.register(NameHandler.class);
        MinecraftForge.EVENT_BUS.register(SignalBoxHandler.class);
        debug = true;
        log = LoggerContext.getContext().getLogger(MODID);
        contentPacks = new FileReader(MODID, "assets/" + MODID, log, name -> {
            final Optional<Path> path = getRessourceLocation(name);
            if (path.isPresent())
                return path.get().toAbsolutePath();
            return Paths.get("");
        });
    }

    @SidedProxy(serverSide = "com.troblecodings.signals.proxy.CommonProxy",
            clientSide = "com.troblecodings.signals.proxy.ClientProxy")
    public static CommonProxy proxy;
    private static Logger log = null;
    public static GuiHandler handler = null;
    public static NetworkHandler network = null;
    public static FileReader contentPacks = null;
    private static boolean debug;

    /**
     * @return the debug
     */
    public static boolean isDebug() {
        return debug;
    }

    @EventHandler
    public void preinit(final FMLPreInitializationEvent event) {
        debug = Files.isDirectory(event.getSourceFile().toPath());
        log = event.getModLog();
        proxy.initModEvent(event);
    }

    @EventHandler
    public void init(final FMLInitializationEvent event) {
        proxy.init(event);
    }

    public static Logger getLogger() {
        if (log == null)
            log = LogManager.getLogger(MODID);
        return log;
    }

    private static FileSystem fileSystemCache;

    public static Optional<Path> getRessourceLocation(final String location) {
        String filelocation = location;
        final URL url = OSBlocks.class.getResource("/assets/" + MODID);
        try {
            if (url != null) {
                final URI uri = url.toURI();
                if ("file".equals(uri.getScheme())) {
                    if (!location.startsWith("/"))
                        filelocation = "/" + filelocation;
                    final URL resource = OSBlocks.class.getResource(filelocation);
                    if (resource == null)
                        return Optional.empty();
                    return Optional.of(Paths.get(resource.toURI()));
                } else {
                    if (!"jar".equals(uri.getScheme())) {
                        return Optional.empty();
                    }
                    if (fileSystemCache == null) {
                        fileSystemCache = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    }
                    return Optional.of(fileSystemCache.getPath(filelocation));
                }
            }
        } catch (final IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static void addToFileSystem(final FileSystem system) {
        if (fileSystemCache == null)
            getRessourceLocation("");

        final URL url = OSBlocks.class.getResource("/assets/opensignals");
        try {
            final URI uri = url.toURI();
            final String scheme = uri.getScheme();
            Path path = null;
            if (scheme.equals("file")) {
                path = fileSystemCache.provider().getPath(uri);
            } else if (scheme.equals("jar")) {
                path = fileSystemCache.getPath("/");
            }
            if (path == null) {
                OpenSignalsMain.getLogger()
                        .error("[Error]: Could not get path to add to file system!");
                return;
            }
            final Path finalPath = path;
            Files.walkFileTree(system.getPath("/"), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                        throws IOException {
                    final Path nextPath = finalPath.resolve(file.getFileName().toString());
                    ByteStreams.copy(Files.newInputStream(file), Files.newOutputStream(nextPath));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (final URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }
}