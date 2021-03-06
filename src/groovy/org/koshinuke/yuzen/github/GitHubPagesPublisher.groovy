package org.koshinuke.yuzen.github




import groovy.io.FileType
import groovy.io.FileVisitResult

import java.nio.file.Path

import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand.ListMode
import org.eclipse.jgit.dircache.DirCacheBuilder
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.util.FileUtils
import org.koshinuke.jgit.CreateOrphanBranchCommand
import org.koshinuke.jgit.GGitUtil
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
		cmd.setNoCheckout(true)
		cmd.call()
	}

	def checkout(Git git) {
		List<Ref> refs = git.branchList().setListMode(ListMode.ALL).call().findAll { it.name.endsWith(PAGES) }
		if(refs.isEmpty()) {
			def cmd = new CreateOrphanBranchCommand(git.getRepository())
			cmd.setName(PAGES).call()
			deleteFiles(git)
		} else {
			Ref ref = refs.find { it.name.startsWith(Constants.R_HEADS) }
			def cmd = git.checkout().setName(PAGES)
			if(ref == null) {
				cmd.setCreateBranch(true).setStartPoint(refs[0].name)
			}
			cmd.call()
		}
	}

	def deleteFiles(Git git) {
		GGitUtil.lockDirCache(git.getRepository()) {
			DirCacheBuilder builder = it.builder()
			this.workingDir.eachFileMatch({ it != '.git' },{
				FileUtils.delete(it, FileUtils.RECURSIVE)
			})
			builder.commit()
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
