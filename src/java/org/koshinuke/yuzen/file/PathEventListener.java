package org.koshinuke.yuzen.file;

import java.io.IOException;
import java.util.EventListener;

/**
 * @author taichi
 */
public interface PathEventListener extends EventListener {

	void overflowed() throws IOException;

	void created(PathEvent event) throws IOException;

	void deleted(PathEvent event) throws IOException;

	void modified(PathEvent event) throws IOException;
}
