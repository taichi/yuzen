package org.koshinuke.yuzen.gradle;

import static org.junit.Assert.*;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before;
import org.junit.Test
import org.koshinuke.yuzen.TestData
import org.koshinuke.yuzen.publish.FTPSTestingSupport;

/**
 * @author taichi
 */
class PublishTaskTest {
	Project project

	FTPSTestingSupport ftps

	@Before
	void setUp() {
		this.project = ProjectBuilder.builder().build()
		TestData.overwrite(this.project.ext)
		project.apply plugin: 'yuzen'

		this.ftps = new FTPSTestingSupport()
		this.ftps.initialize()
	}

	@After
	void tearDown() {
		this.ftps.dispose()
	}

	@Test
	void publish() {
		this.project.yuzen.publish {
			ftps {
				host = "localhost"
				username = ftps.user.name
				password = ftps.user.password
			}
		}

		File root = new File("test/resources/taskconsumer/_contents")
		def publish = project.tasks.publish
		publish.rootDir = root
		publish.execute()

		File home = new File(this.ftps.ftpDir, this.ftps.user.homeDirectory)
		assert home.exists()

		assert new File(home, "profile.md").exists()
		assert new File(home, "entry/2012-04-29/staticcontents.md").exists()
	}
}
