package org.koshinuke.yuzen.github;

import static org.junit.Assert.*;


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

	GhPagesTestingSupport support

	@Before
	void setUp() {
		this.support = new GhPagesTestingSupport()
		this.support.initialize()

		target = new GitHubPagesPublisher()
		target.repoURI = new File(support.repoDir, ".git").toURI().toASCIIString()
		target.workingDir = support.workingDir
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
		support.dispose()
	}

	@Test
	void publish() {
		support.makeFiles(support.rootDir, ["aaa/bbb/ccc", "aaa/bbb/ddd"])
		this.target.publish(support.rootDir)
		Git git = Git.open(new File(support.repoDir, ".git"))

		GGitUtil.handle(git) {
			git.checkout().setName(GitHubPagesPublisher.PAGES).call()

			File dir = new File(support.repoDir, "aaa/bbb")
			assert 2 == dir.list().length
		}
	}

	@Test
	void overwrite() {
		Git git = Git.open(new File(support.repoDir, ".git"))
		GGitUtil.handle(git) {
			git.checkout().setCreateBranch(true).setName(GitHubPagesPublisher.PAGES).call()
			makeFiles(support.repoDir, ["aaa/bbb/ccc", "aaa/bbb/ddd"])
			git.add().addFilepattern(".").call()
			git.commit().setMessage("aaa").call()
			git.checkout().setName("master").call()

			makeFiles(support.rootDir, ["aaa/bbb/ccc", "aaa/bbb/ddd"])
			this.target.publish(support.rootDir)

			git.checkout().setName(GitHubPagesPublisher.PAGES).call()

			File dir = new File(support.repoDir, "aaa/bbb")
			assert 2 == dir.list().length
		}
	}
}
