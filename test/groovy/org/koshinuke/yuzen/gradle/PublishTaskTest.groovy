package org.koshinuke.yuzen.gradle;

import static org.junit.Assert.*;

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.util.FileUtils
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before;
import org.junit.Test
import org.koshinuke.jgit.GGitUtil;
import org.koshinuke.yuzen.TestData
import org.koshinuke.yuzen.github.GhPagesTestingSupport
import org.koshinuke.yuzen.publish.FTPSTestingSupport;

/**
 * @author taichi
 */
class PublishTaskTest {
	Project project

	@Before
	void setUp() {
		this.project = ProjectBuilder.builder().build()
		TestData.overwrite(this.project.ext)
		project.apply plugin: 'yuzen'
	}

	@After
	void tearDown() {
		FileUtils.delete(this.project.getProjectDir(), FileUtils.RECURSIVE)
	}

	@Test
	void ftps() {
		def support = new FTPSTestingSupport()
		try {
			support.initialize()

			this.project.yuzen.publish {
				ftps {
					host = "localhost"
					username = support.user.name
					password = support.user.password
				}
			}

			File root = new File("test/resources/taskconsumer/_contents")
			def publish = project.tasks.publish
			publish.rootDir = root
			publish.execute()

			File home = new File(support.ftpDir, support.user.homeDirectory)
			assert home.exists()

			assert new File(home, "profile.md").exists()
			assert new File(home, "entry/2012-04-29/staticcontents.md").exists()
		} finally {
			support.dispose()
		}
	}

	@Test
	void ghpages() {
		def support = new GhPagesTestingSupport()
		try {
			support.initialize()

			this.project.yuzen.publish {
				ghpages	repoURI : new File(support.repoDir, ".git").toURI().toASCIIString()
			}

			File root = new File("test/resources/taskconsumer/_contents")
			def publish = project.tasks.publish
			publish.rootDir = root
			publish.execute()

			Git g = Git.open(new File(support.repoDir, ".git"))
			GGitUtil.handle(g) {
				g.checkout().setName("gh-pages").call()
				assert new File(support.repoDir, "profile.md").exists()
				assert new File(support.repoDir, "entry/2012-04-29/staticcontents.md").exists()
			}
		} finally {
			support.dispose()
		}
	}
}
