package org.koshinuke.yuzen.gradle;

import static org.junit.Assert.*;

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test


/**
 * @author taichi
 */
class YuzenPluginTest {

	Project project

	@Before
	void setUp() {
		this.project = ProjectBuilder.builder().build()
		project.apply plugin: 'yuzen'

		def f = this.project.file("_contents/hoge/moge/piro.md")
		f.parentFile.mkdirs()
		f.text = "* testdata"
	}

	@Test
	void defaultConfigureTest() {
		assert new File(project.buildDir,'yuzen/blog') == project.tasks.blog.destinationDir
	}

	@Test
	void userConfigureTest() {
		def f = project.file("hoge/moge/blog")
		project.tasks.blog.destinationDir = f
		assert f == project.tasks.blog.destinationDir
	}

	@Test
	void overwriteTest() {
		def f = project.file("hoge/moge/blog")
		project.tasks.blog.destinationDir = f
		project.task([overwrite:true, type: BlogTask],'blog', { it.destinationDir = f })
		assert f == project.tasks.blog.destinationDir
	}

	@Test
	void executeTest() {
		project.tasks.blog.execute()
	}
}
