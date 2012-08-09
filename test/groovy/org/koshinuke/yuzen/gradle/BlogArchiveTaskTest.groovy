package org.koshinuke.yuzen.gradle

import java.text.SimpleDateFormat;

import org.eclipse.jgit.util.FileUtils
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before;
import org.junit.Test
import org.koshinuke.yuzen.TestData

/**
 * @author taichi
 */
class BlogArchiveTaskTest {

	Project project

	@Before
	void setUp() {
		this.project = ProjectBuilder.builder().build()

		TestData.overwrite(this.project.ext)

		this.project.apply plugin: 'yuzen'

		def today = new SimpleDateFormat('yyyyMMdd').parse("20120405")
		14.times {
			def f = this.project.file("_contents/entry/hoehoe${it}.md")
			f.parentFile.mkdirs()
			f.text = "# aaaaa$it \n* testdata\n* testtest$it"
			f.lastModified = today.minus(7 * it).time
		}

		def src = new File("src/resources/_templates/blog/")
		assert src.exists()
		project.copy {
			from src.toURI().path
			into project.file('_templates/')
		}
	}

	@After
	void tearDown() {
		FileUtils.delete(this.project.getProjectDir(), FileUtils.RECURSIVE)
	}

	@Test
	void test() {
		def archives = this.project.tasks.blogArchives
		archives.execute()

		def f = new File(archives.getDestinationDir(), 'archives/index.html')
		assert f.exists()
	}
}
