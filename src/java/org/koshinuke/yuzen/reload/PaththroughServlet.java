package org.koshinuke.yuzen.reload;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.koshinuke.yuzen.reload.PaththroughServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author taichi
 */
public class PaththroughServlet extends WebSocketServlet {

	static final Logger LOG = LoggerFactory.getLogger(PaththroughServlet.class);

	private static final long serialVersionUID = -5354833775183668547L;

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
			PaththroughServlet.this.sockets.add(this);
		}

		@Override
		public void onClose(int closeCode, String message) {
			LOG.debug("onClose {} {}", closeCode, message);
			PaththroughServlet.this.sockets.remove(this);
		}

		@Override
		public void onMessage(String data) {
			LOG.debug("onMessage {}", data);
			for (PaththroughSocket rs : PaththroughServlet.this.sockets) {
				try {
					rs.connection.sendMessage(data);
				} catch (IOException e) {
					LOG.warn(e.getMessage(), e);
				}
			}
		}
	}
}
