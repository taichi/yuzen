package org.koshinuke.yuzen.gradle;

import static org.junit.Assert.*;

import java.io.File

import org.eclipse.jgit.util.FileUtils;
import org.gradle.api.Project
import org.gradle.api.Task;
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

		def f = this.project.file("_contents/entry/moge/piro.md")
		f.parentFile.mkdirs()
		f.text = "* testdata"

		f = this.project.file("_contents/profile.md")
		f.text = "# profile\n* profile profile"

		f = this.project.file("_contents/moge.markdown")
		f.text = "zzzz"

		f = this.project.file("_contents/entry/moge/fuga.txt")
		f.parentFile.mkdirs()
		f.text = "yyyy"

		f = this.project.file("$project.buildDir/yuzen/blog")
		FileUtils.delete(f, FileUtils.RECURSIVE | FileUtils.SKIP_MISSING)
		f.mkdirs()
	}

	@Test
	void defaultConfigureTest() {
		assert new File(project.buildDir,'yuzen/blog') == project.tasks.blog.destinationDir
	}

	@Test
	void overwriteTest() {
		def f = project.file("hoge/moge/blog")
		project.tasks.blog.destinationDir = f
		project.task([overwrite:true, type: BlogTask],'blog', { it.destinationDir = f })
		assert f == project.tasks.blog.destinationDir
	}

	@Test
	void executeBlogTest() {
		project.tasks.blog.execute()
		def dest = project.file("$project.buildDir/yuzen/blog/entry/moge/piro/index.html")
		assert dest.exists()
		println dest.text

		def profile = project.file("$project.buildDir/yuzen/blog/profile/index.html")
		assert profile.exists()
		println profile.text

		def markdown = project.file("$project.buildDir/yuzen/blog/moge.markdown")
		assert markdown.exists()

		def txt = project.file("$project.buildDir/yuzen/blog/entry/moge/fuga.txt")
		assert txt.exists()
	}

	@Test
	void executeServerTask() {
		Task task = project.tasks.findByName('startBlog')
		assert task != null
		try {
			task.bootServer()
			def actf = this.project.file("_contents/entry/moge/piro.md")
			actf.parentFile.mkdirs()
			actf.text = "** test test"
			def dest = project.file("$project.buildDir/yuzen/blog/entry/moge/piro/index.html")
			for(int i=0; i < 1000; i++) {
				Thread.sleep(10)
				if(dest.exists() && 0 < dest.length()) {
					break
				}
			}
			assert dest.exists()
			assert 0 < dest.length()
		} finally {
			task.stopServer()
		}
	}

	@Test
	void copyResource() {
		Task task = project.tasks.findByName('startBlog')
		assert task != null
		try {
			task.bootServer()
			File actf = this.project.file("_contents/entry/moge/fuga.txt")
			actf.text = "** test test"
			def dest = project.file("$project.buildDir/yuzen/blog/entry/moge/fuga.txt")
			for(int i=0; i < 100; i++) {
				Thread.sleep(10)
				if(dest.exists()) {
					return
				}
			}
			assert dest.exists()
		} finally {
			task.stopServer()
		}
	}
}
