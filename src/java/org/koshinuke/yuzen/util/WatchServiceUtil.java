package org.koshinuke.yuzen.util;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author taichi
 */
public class WatchServiceUtil {

	static final Logger LOG = LoggerFactory.getLogger(WatchServiceUtil.class);

	static final WatchEvent.Modifier FILE_TREE;

	static {
		FILE_TREE = findFileTree();
	}

	static Modifier findFileTree() {
		try {
			Class<?> clazz = Class
					.forName("com.sun.nio.file.ExtendedWatchEventModifier");
			for (Object o : clazz.getEnumConstants()) {
				Modifier mod = (Modifier) o;
				if ("FILE_TREE".equals(mod.name())) {
					return mod;
				}
			}
		} catch (ClassNotFoundException e) {
			// nop
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return null;
	}

	public static WatchService newWatchService() {
		try {
			return FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	public static void watchAll(@Nonnull final WatchService ws,
			@Nonnull Path root) {
		Objects.requireNonNull(ws);
		Objects.requireNonNull(root);
		try {
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir,
						BasicFileAttributes attrs) throws IOException {
					watch(ws, dir);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	public static void watchTree(@Nonnull final WatchService ws,
			@Nonnull Path root) {
		if (FILE_TREE == null) {
			watchAll(ws, root);
		} else {
			watch(ws, root, FILE_TREE);
		}
	}

	public static WatchKey watch(@Nonnull WatchService ws, @Nonnull Path dir,
			WatchEvent.Modifier... modifiers) {
		Objects.requireNonNull(ws);
		Objects.requireNonNull(dir);
		LOG.debug("watch {}", dir);
		try {
			return dir.register(ws, new WatchEvent.Kind<?>[] { ENTRY_CREATE,
					ENTRY_DELETE, ENTRY_MODIFY, OVERFLOW }, modifiers);
		} catch (IOException e) {
			throw new IORuntimeException(e);
		}
	}

	public static void close(WatchService ws) {
		try {
			if (ws != null) {
				ws.close();
			}
		} catch (IOException e) {
			LOG.warn(e.getMessage(), e);
		}
	}

}
