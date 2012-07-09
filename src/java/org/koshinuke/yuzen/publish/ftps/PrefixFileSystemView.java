package org.koshinuke.yuzen.publish.ftps;

import java.io.File;

import org.apache.ftpserver.filesystem.nativefs.impl.NativeFileSystemView;
import org.apache.ftpserver.filesystem.nativefs.impl.NativeFtpFile;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.koshinuke.yuzen.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author taichi
 * @see NativeFileSystemView
 */
public class PrefixFileSystemView implements FileSystemView {

	private static final Logger LOG = LoggerFactory
			.getLogger(PrefixFileSystemView.class);

	protected File prefix;
	protected String rootDir;
	protected String currDir;
	protected User user;
	protected boolean caseInsensitive;

	public PrefixFileSystemView(File prefix, User user, boolean caseInsensitive) {
		if (user.getHomeDirectory() == null) {
			throw new IllegalArgumentException(
					"User home directory can not be null");
		}

		this.prefix = prefix;
		this.caseInsensitive = caseInsensitive;
		String rootDir = user.getHomeDirectory();
		rootDir = NativeFtpFile.normalizeSeparateChar(rootDir);
		rootDir = this.appendSlash(rootDir);
		LOG.debug(Markers.LIFECYCLE,
				"Filesystem view created for user {} with root {}",
				user.getName(), rootDir);

		this.rootDir = rootDir;
		this.user = user;
		this.currDir = "/";
	}

	class PrefixFtpFile extends NativeFtpFile {
		public PrefixFtpFile(final String fileName, final String file,
				final User user) {
			super(fileName, new File(PrefixFileSystemView.this.prefix, file),
					user);
			LOG.debug(Markers.LIFECYCLE, "user {} FileName {} realPath {}",
					new Object[] { user.getName(), fileName, file });
		}
	}

	@Override
	public FtpFile getHomeDirectory() {
		return new PrefixFtpFile("/", this.rootDir, this.user);
	}

	@Override
	public FtpFile getWorkingDirectory() {
		if ("/".equals(this.currDir)) {
			return this.getHomeDirectory();
		}
		String path = this.rootDir + this.currDir.substring(1);
		return new PrefixFtpFile(this.currDir, path, this.user);
	}

	@Override
	public FtpFile getFile(String file) {
		String physicalName = this.toPhysicalName(file);
		String userFileName = physicalName.substring(this.rootDir.length() - 1);
		return new PrefixFtpFile(userFileName, physicalName, this.user);
	}

	protected String toPhysicalName(String path) {
		return NativeFtpFile.getPhysicalName(this.rootDir, this.currDir, path,
				this.caseInsensitive);
	}

	protected String appendSlash(String path) {
		if (path.endsWith("/") == false) {
			return path + "/";
		}
		return path;
	}

	@Override
	public boolean changeWorkingDirectory(String dir) {
		String physicalName = this.toPhysicalName(dir);
		File maybeDir = new File(this.prefix, physicalName);
		if (maybeDir.isDirectory()) {
			String striped = physicalName.substring(this.rootDir.length() - 1);
			this.currDir = this.appendSlash(striped);
			return true;
		}
		return false;
	}

	@Override
	public boolean isRandomAccessible() throws FtpException {
		return true;
	}

	@Override
	public void dispose() {
		LOG.debug(Markers.LIFECYCLE,
				"Filesystem view disposed for user {} with root {}",
				this.user.getName(), this.rootDir);
	}
}
