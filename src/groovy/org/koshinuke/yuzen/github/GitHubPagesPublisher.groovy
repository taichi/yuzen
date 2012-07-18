package org.koshinuke.yuzen.github




import groovy.io.FileType
import groovy.io.FileVisitResult

import java.io.File
import java.nio.file.Path;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.ListBranchCommand.ListMode
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.koshinuke.jgit.CreateOrphanBranchCommand
import org.koshinuke.jgit.GGitUtil;
import org.koshinuke.jgit.PassphraseProvider
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
	def File workingDir

	def String username
	def String password

	def credentials = [null : { new PassphraseProvider() },  'https': { new UsernamePasswordCredentialsProvider(this.username, this.password) }]

	@Override
	public void publish(File rootDir) {
		File dir = this.workingDir
		File workingRepo = new File(dir, ".git")
		Git git = workingRepo.exists() ? Git.open(dir) : cloneRepo(dir)
		GGitUtil.handle(git) {
			checkout(git)
			copyDirs(rootDir, dir)
			git.add().addFilepattern(".").call()
			git.commit().setMessage(getUpdateMessage()).call()
			git.push().add(PAGES).setCredentialsProvider(detectCredentialsProvider()).call()
		}
	}

	def cloneRepo(File workingDir) {
		workingDir.mkdirs()
		CloneCommand cmd = Git.cloneRepository()
		cmd.setURI(this.repoURI)
		cmd.setDirectory(workingDir)
		cmd.call()
	}

	def checkout(Git git) {
		Ref ref = git.branchList().setListMode(ListMode.REMOTE).call().find { it.getName().endsWith(PAGES) }
		if(ref == null) {
			def cmd = new CreateOrphanBranchCommand(git.getRepository())
			cmd.setName(PAGES)
			cmd.call()
		} else {
			StoredConfig config = git.getRepository().getConfig()
			config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, PAGES, ConfigConstants.CONFIG_KEY_MERGE, Constants.R_HEADS + PAGES)
			config.save()
			git.checkout().setName(PAGES).setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM).setCreateBranch(true).call()
			git.pull().call()
		}
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

	def detectCredentialsProvider() {
		URIish uri = new URIish(this.repoURI)
		def c = credentials[uri.scheme]
		if(c != null) {
			return c()
		}
		return null
	}
}
