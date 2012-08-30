package org.koshinuke.yuzen.publish

import java.nio.file.Files;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.ssl.SslConfigurationFactory
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.WritePermission
import org.eclipse.jgit.util.FileUtils;
import org.koshinuke.yuzen.ftpserver.PrefixFileSystemFactory;



/**
 * @author taichi
 */
class FTPSTestingSupport {

	def tmpDir
	def ftpDir
	FtpServer server
	User user

	def initialize() {
		this.tmpDir = Files.createTempDirectory("ftps-test").toFile()
		FtpServerFactory serverFactory = new FtpServerFactory()

		setUpSSL(serverFactory)
		setUpUsers(serverFactory)

		this.ftpDir = new File(tmpDir, "dataDir")
		ftpDir.mkdirs()
		serverFactory.fileSystem = new PrefixFileSystemFactory(ftpDir)

		server = serverFactory.createServer()
		server.start()
	}

	protected setUpSSL(FtpServerFactory serverFactory) {
		ListenerFactory factory = new ListenerFactory()
		SslConfigurationFactory ssl = new SslConfigurationFactory()
		// ユニットテスト用の自己署名キーストア
		ssl.setKeystoreFile(new File("test/resources/ftps.jks"))
		ssl.setKeystorePassword("password")
		factory.setSslConfiguration(ssl.createSslConfiguration())
		//factory.setImplicitSsl(true)
		serverFactory.addListener("default", factory.createListener())
	}

	protected setUpUsers(FtpServerFactory serverFactory) {
		def passwd = new File(this.tmpDir, "users.properties")
		passwd.createNewFile()
		PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory()
		userManagerFactory.setFile(passwd)
		userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor())
		UserManager um = userManagerFactory.createUserManager()
		serverFactory.userManager = um

		this.user = new BaseUser()
		user.setName("testUser")
		user.setPassword("testPass")
		user.setAuthorities([new WritePermission()])
		user.setHomeDirectory("testHome")
		um.save(user)
	}

	def dispose() {
		this.server.stop()
		FileUtils.delete(this.tmpDir, FileUtils.RECURSIVE)
	}
}
