package org.koshinuke.yuzen.reload;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket
import org.eclipse.jetty.websocket.WebSocketClient
import org.eclipse.jetty.websocket.WebSocketClientFactory
import org.junit.After;
import org.junit.AfterClass
import org.junit.Before;
import org.junit.BeforeClass
import org.junit.Test;
import org.koshinuke.yuzen.reload.PaththroughServlet;

/**
 * @author taichi
 */
class PaththroughServletTest {

	static Server server
	static URI serverUri

	WebSocketClientFactory factory
	WebSocketClient client

	@BeforeClass
	static void setUpClass() {
		server = new Server(0)
		ServletContextHandler context = new ServletContextHandler()
		context.contextPath = '/'
		server.handler = context
		context.addServlet(PaththroughServlet, "/*")

		server.start()

		Connector c = server.getConnectors()[0]
		def host = c.host
		host = host == null ? 'localhost' : host
		serverUri = new URI("ws://$host:$c.localPort")
		println "Server URI: $serverUri"
	}

	@AfterClass
	static void tearDownClass() {
		server.stop()
	}

	@Before
	void setUp() {
		this.factory = new WebSocketClientFactory()
		this.factory.start()
		this.client = factory.newWebSocketClient()
	}

	@After
	void tearDown() {
		this.factory.stop()
	}

	@Test
	void sendTest() {
		CountDownLatch latch = new CountDownLatch(2)
		def expected = "hogehoge"
		def code = 0
		def msg = ""
		Future<Connection> future = client.open(serverUri, [
					onOpen: {
					},
					onClose: { c, m ->
						code = c
						latch.countDown()
					},
					onMessage : {
						msg = it
						latch.countDown()
					}
				] as WebSocket.OnTextMessage)
		Connection c = future.get(1, TimeUnit.SECONDS)
		c.sendMessage(expected)
		c.close()
		assert latch.await(1, TimeUnit.SECONDS)
		assert 1000 == code
		assert expected == msg
	}

	@Test
	void receiveTest() {
		CountDownLatch latch = new CountDownLatch(4)
		def expected = "hogehoge"
		def msg = []
		def connects = []

		(latch.count / 2).times {
			Future<Connection> future = client.open(serverUri, [
						onOpen: {
						},
						onClose: { c, m ->
							latch.countDown()
						},
						onMessage : {
							msg.add(it)
							latch.countDown()
						}
					] as WebSocket.OnTextMessage)
			connects.add future.get(1, TimeUnit.SECONDS)
		}

		Connection c = connects[0]
		c.sendMessage(expected)

		connects.each { it.close() }

		assert latch.await(1, TimeUnit.SECONDS)
		assert [expected, expected]== msg
	}
}
