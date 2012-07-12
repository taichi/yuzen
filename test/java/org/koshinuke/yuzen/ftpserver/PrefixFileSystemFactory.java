package org.koshinuke.yuzen.ftpserver;

import java.io.File;

import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.koshinuke.yuzen.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author taichi
 * @see NativeFileSystemFactory
 */
public class PrefixFileSystemFactory implements FileSystemFactory {

	static final Logger LOG = LoggerFactory
			.getLogger(PrefixFileSystemFactory.class);

	File prefix;

	boolean caseInsensitive = false;

	public PrefixFileSystemFactory(File prefix) {
		this(prefix, false);
	}

	public PrefixFileSystemFactory(File prefix, boolean caseInsensitive) {
		this.caseInsensitive = caseInsensitive;
		if (prefix.exists() == false && prefix.mkdirs() == false) {
			throw new IllegalArgumentException(prefix.getAbsolutePath()
					+ " is not accessible.");
		}
		this.prefix = prefix;
	}

	@Override
	public FileSystemView createFileSystemView(User user) throws FtpException {
		synchronized (user) {
			String home = user.getHomeDirectory();
			File dir = new File(this.prefix, home);
			if (dir.isFile()) {
				String msg = "Not a directory :: " + home;
				LOG.warn(Markers.BOUNDARY, msg);
				throw new FtpException(msg);
			}
			if (dir.exists() == false && dir.mkdirs() == false) {
				String msg = "Cannot create user home :: " + home;
				LOG.warn(Markers.BOUNDARY, msg);
				throw new FtpException(msg);
			}
			return new PrefixFileSystemView(this.prefix, user,
					this.caseInsensitive);
		}
	}

}
