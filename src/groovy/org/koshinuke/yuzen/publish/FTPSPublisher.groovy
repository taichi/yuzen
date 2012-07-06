package org.koshinuke.yuzen.publish

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPSClient;
import org.koshinuke.yuzen.Markers;
import org.koshinuke.yuzen.util.FileUtil;
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

/**
 * @author taichi
 */
class FTPSPublisher implements Publisher {

	static final Logger LOG = LoggerFactory.getLogger(FTPSPublisher)

	def String protocol = FTPSClient.DEFAULT_PROTOCOL
	def boolean isImplicit = false
	def String host
	def String username
	def String password

	def String dirPrefix

	@Override
	public void publish(File rootDir) {
		def FTPSClient ftps = new FTPSClient(this.protocol, this.isImplicit)
		try {
			LOG.debug(Markers.BOUNDARY, "connect to $host")
			ftps.connect(this.host)
			if(ftps.login(this.username, this.password)) {
				ftps.enterLocalPassiveMode()
				transferFiles(ftps)
				ftps.noop()
				ftps.logout()
			} else {
				def s = "login failed. username/password is incorrect."
				LOG.error(Markers.HELP, s)
				throw new IllegalStateException(s)
			}
		} finally {
			LOG.debug(Markers.BOUNDARY, "disconnect from $host")
			if(ftps.isConnected()) {
				ftps.disconnect()
			}
		}
	}

	def transferFiles(FTPSClient ftps, File rootDir) {
		// TODO MIME type handling ?
		ftps.setFileType(FTP.BINARY_FILE_TYPE)

		final Path root = rootDir.toPath().toAbsolutePath()
		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult preVisitDirectory(Path dir,
					BasicFileAttributes attrs) throws IOException {
						def p = FileUtil.slashify(root.relativize(dir))
						def path = "$dirPrefix/$p"
						LOG.debug(Markers.BOUNDARY, "CWD $path")
						if(ftps.changeWorkingDirectory(path) == false) {
							LOG.debug(Markers.BOUNDARY, "MKD $path")
							if(ftps.makeDirectory(path)) {
								if(ftps.changeWorkingDirectory(path) == false) {
									LOG.error(Markers.BOUNDARY, "fail to access new remote directory.")
									return FileVisitResult.SKIP_SUBTREE
								}
							} else {
								LOG.error(Markers.BOUNDARY, "fail to make remote directory.")
								return FileVisitResult.SKIP_SUBTREE
							}
						}
						return FileVisitResult.CONTINUE
					}
					@Override
					public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
						def p = FileUtil.slashify(root.relativize(file))
						def path = "$dirPrefix/$p"
						def local = file.toFile()
						local.withInputStream {
							LOG.debug(Markers.BOUNDARY, "STOR $path $file")
							if(ftps.storeFile(path, it) == false) {
								LOG.warn(Markers.BOUNDARY, "fail to store file.")
							}
						}
						return FileVisitResult.CONTINUE;
					}
				})
	}
}
