package org.koshinuke.yuzen.util;

import org.eclipse.jetty.websocket.WebSocket;

/**
 * @author taichi
 */
public class WebSocketUtil {

	public static void close(WebSocket.Connection c) {
		if (c != null) {
			c.close();
		}
	}
}
