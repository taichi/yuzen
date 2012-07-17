package org.koshinuke.yuzen.github;

import static org.junit.Assert.*;

import java.nio.file.Files

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.util.FileUtils
import org.junit.After;
import org.junit.Before
import org.junit.Test;
import org.koshinuke.jgit.GGitUtil;

/**
 * @author taichi
 */
class GitHubPagesPublisherTest {

	GitHubPagesPublisher target

	File tmpDir

	File repoDir

	File workingDir

	File rootDir

	@Before
	void setUp() {
		this.tmpDir = Files.createTempDirectory("ghpages-test").toFile()
		def mkdir = {
			def d = new File(tmpDir,it)
			d.mkdirs()
			return d
		}
		this.repoDir = mkdir("repo")
		this.workingDir = mkdir("working")
		this.rootDir = mkdir("build")

		Git g = Git.init().setDirectory(this.repoDir).call()
		GGitUtil.handle(g) {
			makeFiles(repoDir, ["aaa", "bbb", "ccc"])

			g.add().addFilepattern(".").call()
			g.commit().setMessage("commit message").call()
		}

		target = new GitHubPagesPublisher()
		target.repoURI = new File(this.repoDir, ".git").toURI().toASCIIString()
		target.workingDir = this.workingDir
	}

	def makeFiles(dir, names) {
		names.each {
			def f = new File(dir, "${it}.txt")
			f.parentFile.mkdirs()
			f.text = "${it} ${new Date()}"
		}
	}

	@After
	void tearDown() {
		FileUtils.delete(this.tmpDir, FileUtils.RECURSIVE)
	}

	@Test
	void publish() {
		makeFiles(this.rootDir, ["aaa/bbb/ccc", "aaa/bbb/ddd"])
		this.target.publish(this.rootDir)
		Git git = Git.open(new File(this.repoDir, ".git"))

		GGitUtil.handle(git) {
			git.checkout().setName(GitHubPagesPublisher.PAGES).call()

			File dir = new File(this.repoDir, "aaa/bbb")
			assert 2 == dir.list().length
		}
	}

	@Test
	void overwrite() {
		Git git = Git.open(new File(this.repoDir, ".git"))
		GGitUtil.handle(git) {
			git.checkout().setCreateBranch(true).setName(GitHubPagesPublisher.PAGES).call()
			makeFiles(this.repoDir, ["aaa/bbb/ccc", "aaa/bbb/ddd"])
			git.add().addFilepattern(".").call()
			git.commit().setMessage("aaa").call()
			git.checkout().setName("master").call()

			makeFiles(this.rootDir, ["aaa/bbb/ccc", "aaa/bbb/ddd"])
			this.target.publish(this.rootDir)

			git.checkout().setName(GitHubPagesPublisher.PAGES).call()

			File dir = new File(this.repoDir, "aaa/bbb")
			assert 2 == dir.list().length
		}
	}
}
