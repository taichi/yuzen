package org.koshinuke.yuzen.github;

import static org.junit.Assert.*;

import java.nio.file.Files

import org.eclipse.jgit.api.Git;
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
			["aaa", "bbb", "ccc"].each {
				def f = new File(repoDir, "${it}.txt")
				f.text = it
			}
			g.add().addFilepattern(".").call()
			g.commit().setMessage("commit message").call()
		}

		["ccc", "ddd"].each {
			def f = new File(rootDir, "aaa/bbb/${it}.txt")
			f.parentFile.mkdirs()
			f.text = it
		}

		target = new GitHubPagesPublisher()
		target.repoURI = new File(this.repoDir, ".git").toURI().toASCIIString()
		target.workingDir = this.workingDir
	}

	@After
	void tearDown() {
		//FileUtils.delete(this.tmpDir, FileUtils.RECURSIVE)
	}

	@Test
	void publish() {
		this.target.publish(this.rootDir)
	}
}
