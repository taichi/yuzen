package org.koshinuke.yuzen.file;

import java.io.IOException;

/**
 * @author taichi
 */
public interface PathEventDispatcher {

	void dispatch(PathEventListener listener, PathEvent event)
			throws IOException;
}
