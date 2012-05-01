package org.koshinuke.yuzen.file;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * @author taichi
 */
public interface PathEvent {

	WatchEvent.Kind<?> getKind();

	Path getPath();

}
