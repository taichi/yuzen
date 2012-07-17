package org.koshinuke.yuzen.github




import groovy.io.FileType
import groovy.io.FileVisitResult

import java.io.File
import java.nio.file.Path;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.transport.CredentialsProvider
import org.koshinuke.jgit.CreateOrphanBranchCommand
import org.koshinuke.jgit.GGitUtil;
import org.koshinuke.yuzen.publish.Publisher

import com.google.common.io.Files

/**
 * @author taichi
 */
class GitHubPagesPublisher implements Publisher {

	static final String PAGES = 'gh-pages'

	static final String DEFAULT_UPDATEMESSAGE = "Updated at ${new Date()}"

	def updateMessage = DEFAULT_UPDATEMESSAGE
	def String repoURI
	def CredentialsProvider credentials
	def File workingDir

	@Override
	public void publish(File rootDir) {
		File dir = this.workingDir
		File workingRepo = new File(dir, ".git")
		Git git = workingRepo.exists() ? Git.open(dir) : cloneRepo(dir)
		GGitUtil.handle(git) {
			Ref ref = checkout(git)
			copyDirs(rootDir, dir)
			git.add().addFilepattern(".").call()
			git.commit().setMessage(getUpdateMessage()).call()
			git.push().add(ref).setCredentialsProvider(this.credentials).call()
		}
	}

	def cloneRepo(File workingDir) {
		workingDir.mkdirs()
		CloneCommand cmd = Git.cloneRepository()
		cmd.setURI(this.repoURI)
		cmd.setNoCheckout(true)
		cmd.setDirectory(workingDir)
		cmd.call()
	}

	def checkout(Git git) {
		def cmd;
		if(git.getRepository().getRef(PAGES) == null) {
			cmd = new CreateOrphanBranchCommand(git.getRepository())
		} else {
			cmd = git.checkout()
		}
		cmd.setName(PAGES)
		cmd.call()
	}

	def copyDirs(File srcDir, File destDir) {
		Path rootPath = srcDir.toPath()
		Path destRoot = destDir.toPath()
		srcDir.traverse(type: FileType.FILES, preDir: { if (it.name == '.git') return FileVisitResult.SKIP_SUBTREE }) {
			def p = it.toPath()
			def rel = rootPath.relativize(p)
			def to = destRoot.resolve(rel).toFile()
			to.parentFile.mkdirs()
			Files.copy(it, to)
		}
	}
}
