package org.koshinuke.yuzen.reload;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author taichi
 */
public class PaththroughHandler extends WebSocketHandler {

	static final Logger LOG = LoggerFactory.getLogger(PaththroughHandler.class);

	List<PaththroughSocket> sockets = new CopyOnWriteArrayList<>();

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request,
			String protocol) {
		return new PaththroughSocket();
	}

	class PaththroughSocket implements OnTextMessage {

		Connection connection;

		@Override
		public void onOpen(Connection connection) {
			LOG.debug("onOpen");
			this.connection = connection;
			PaththroughHandler.this.sockets.add(this);
		}

		@Override
		public void onClose(int closeCode, String message) {
			LOG.debug("onClose {} {}", closeCode, message);
			PaththroughHandler.this.sockets.remove(this);
		}

		@Override
		public void onMessage(String data) {
			LOG.debug("onMessage {}", data);
			for (PaththroughSocket rs : PaththroughHandler.this.sockets) {
				try {
					rs.connection.sendMessage(data);
				} catch (IOException e) {
					LOG.warn(e.getMessage(), e);
				}
			}
		}
	}
}
