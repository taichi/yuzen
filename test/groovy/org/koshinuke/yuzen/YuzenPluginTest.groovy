package org.koshinuke.yuzen;

import static org.junit.Assert.*;

import groovy.xml.XmlUtil;

import java.io.File
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jgit.util.FileUtils;
import org.gradle.api.Project
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import org.koshinuke.yuzen.file.PathEventListener
import org.koshinuke.yuzen.gradle.BlogPluginExtension;
import org.koshinuke.yuzen.gradle.DefaultContentsTask;


/**
 * @author taichi
 */
class YuzenPluginTest {

	Project project

	@Before
	void setUp() {
		this.project = ProjectBuilder.builder().build()

		TestData.overwrite(this.project.ext)

		project.apply plugin: 'yuzen'

		def f = this.project.file("_contents/entry/moge/piro.md")
		f.parentFile.mkdirs()
		f.text = "* testdata"

		f = this.project.file("_contents/entry/L'Arc～en～Ciel/2011/12/21/ごが.md")
		f.parentFile.mkdirs()
		f.text = "# ほげほげ\n* ごがごが\n* でででん"

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

		def src = new File("_templates/blog/")
		assert src.exists()
		project.copy {
			from src.toURI().path
			into project.file('_templates/')
		}
	}

	@Test
	void defaultConfigureTest() {
		def blog = project.tasks.blog
		assert project.file('_contents') == blog.contentsDir
		assert new File(project.buildDir,'yuzen') == blog.destinationDir
		assert '_templates' == blog.templatePrefix
		assert '.html' == blog.templateSuffix
	}

	@Test
	void overwriteTest() {
		def f = project.file("hoge/moge/blog")
		project.tasks.blog.destinationDir = f
		project.task([overwrite:true, type: DefaultContentsTask],'blog', { it.destinationDir = f })
		assert f == project.tasks.blog.destinationDir
	}

	@Test
	void executeBlogTest() {
		project.tasks.blog.execute()

		def dest = project.file("$project.buildDir/yuzen/entry/moge/piro/index.html")
		assert dest.exists()
		println dest.text

		def profile = project.file("$project.buildDir/yuzen/profile/index.html")
		assert profile.exists()
		println profile.text

		def markdown = project.file("$project.buildDir/yuzen/moge.markdown")
		assert markdown.exists()

		def txt = project.file("$project.buildDir/yuzen/entry/moge/fuga.txt")
		assert txt.exists()
	}

	@Test
	void executeServerTask() {
		Task task = project.tasks.findByName('startBlog')
		assert task != null
		task.bootServer()
		try {
			def latch = makeLatch(task)
			def actf = this.project.file("_contents/entry/moge/piro.md")
			actf.parentFile.mkdirs()
			actf.text = "** test test"
			def dest = project.file("$project.buildDir/yuzen/entry/moge/piro/index.html")
			assert latch.await(2, TimeUnit.SECONDS)
			assert dest.exists()
			assert 0 < dest.length()
		} finally {
			task.stopServer()
		}
	}

	def makeLatch(task) {
		// TODO PathSentinelのdispatch処理が
		// リスナーをそれぞれスレッドプールにキューイングするまでの暫定処置
		CountDownLatch latch = new CountDownLatch(1)
		task.sentinel.register([
					overflowed : {  },
					created : {
					},
					modified : { latch.countDown() },
					deleted : {
					}
				] as PathEventListener)
		return latch
	}

	@Test
	void copyResource() {
		Task task = project.tasks.findByName('startBlog')
		assert task != null
		task.bootServer()
		try {
			def latch = makeLatch(task)
			File actf = this.project.file("_contents/entry/moge/fuga.txt")
			actf.text = "** test test"
			def dest = project.file("$project.buildDir/yuzen/entry/moge/fuga.txt")
			assert latch.await(2, TimeUnit.SECONDS)
			assert dest.exists()
			assert 0 < dest.length()
		} finally {
			task.stopServer()
		}
	}

	@Test
	void recentPosts() {
		def blog = project.extensions.getByType(BlogPluginExtension)
		def expected = [
			[url:'/entry/L%27Arc%EF%BD%9Een%EF%BD%9ECiel/2011/12/21/%E3%81%94%E3%81%8C', title:'ほげほげ'],
			[url:'/entry/moge/piro', title:'piro'],
			[url:'/profile', title:'profile']
		]

		3.times {
			def actual = blog.recentPosts[it]
			assert expected[it].url == actual.url
			assert expected[it].title == actual.title
			assert actual.timestamp != null
			assert actual.summary != null
			println XmlUtil.serialize(actual.summary)
		}
	}
}
