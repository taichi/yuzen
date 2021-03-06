package org.koshinuke.yuzen

import static org.junit.Assert.*
import groovy.xml.XmlUtil

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import org.eclipse.jgit.util.FileUtils
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ProjectReportsPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koshinuke.yuzen.file.PathEventListener
import org.koshinuke.yuzen.gradle.DefaultContentsTask


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

		def today = new Date()

		def f = this.project.file("_contents/entry/moge/piro.md")
		f.parentFile.mkdirs()
		f.text = "* testdata"
		f.lastModified = (today - 2).time

		f = this.project.file("_contents/entry/L'Arc～en～Ciel/2011/12/21/ごが.md")
		f.parentFile.mkdirs()
		f.text = "# ほげほげ\n* ごがごが\n* でででん"
		f.lastModified = (today - 1).time

		f = this.project.file("_contents/moge.markdown")
		f.text = "zzzz"

		f = this.project.file("_contents/entry/moge/fuga.txt")
		f.parentFile.mkdirs()
		f.text = "yyyy"

		14.times {
			f = this.project.file("_contents/entry/hoehoe${it}.md")
			f.parentFile.mkdirs()
			f.text = "# aaaaa$it \n* testdata\n* testtest$it"
			f.lastModified = today.minus(3 + it).time
		}


		f = this.project.file("$project.buildDir/yuzen/blog")
		FileUtils.delete(f, FileUtils.RECURSIVE | FileUtils.SKIP_MISSING)
		f.mkdirs()

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
	void defaultConfigureTest() {
		def blog = project.tasks.blog
		assert project.fileTree(project.file('_contents'),{}).dir == blog.contents.dir
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
		project.blog.title = 'bbbb tttt'
		project.blog.subtitle = 'sss bbbb tttt'
		project.tasks.blog.execute()

		def dest = project.file("$project.buildDir/yuzen/entry/moge/piro/index.html")
		assert dest.exists()
		println dest.text

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
			assert latch.await(5, TimeUnit.SECONDS)
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
		def blog = project.blog
		def expected = [
			[url:'entry/L%27Arc%EF%BD%9Een%EF%BD%9ECiel/2011/12/21/%E3%81%94%E3%81%8C', title:'ほげほげ'],
			[url:'entry/moge/piro', title:'piro']
		]

		2.times {
			def actual = blog.recentPosts[it]
			assert expected[it].url == actual.url
			assert expected[it].title == actual.title
			assert actual.timestamp != null
			assert actual.summary != null
			println XmlUtil.serialize(actual.summary)
		}
	}

	@Test
	void paging() {
		def paging = project.paging

		assert paging.getTemplatePrefix() != null
		assert paging.getTemplateSuffix() != null
		assert paging.getDestinationDir() != null
		assert paging.pagingPrefix == 'page'
		assert paging.entryDirName == 'entry'

		def s = 'pagepage'
		project.yuzen.pagingPrefix = s
		assert paging.pagingPrefix == s

		paging.execute()
	}

	@Test
	void initTemplate() {
		def init = project.initblog
		init.fromTree = {
			project.fileTree('_templates/', {})
		}
		init.execute()
		def f = new File(project.projectDir, "_templates/page.html")
		assert f.exists()
	}

	@Test
	void index() {
		def indexTemplate = project.file("_templates/index.html")
		indexTemplate.text = '''<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head th:include="fragments/head::${blog.head}">
<meta charset="utf-8" />
<title>Blog Title</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<link rel="stylesheet/less" type="text/css" href="less/main.less" />
<script type="application/javascript" src="less/less-1.3.0.min.js"></script>
</head>
<body>
	<article yz:markdown="${content.path}">てすと</article>
</body></html>
'''
		def indexMD = project.file("_contents/index.md")
		indexMD.text = '''
# head
hogehogefuga

* 1
* 2
* 3
'''
		project.tasks.blog.execute()
		def html = new File(project.yuzen.getDestinationDir(), 'index.html')
		assert html.exists()
	}

	/**
	 * Rule で追加されるTaskに対する依存性があると、gradle tasksでイテレート中に、project.tasksを変更する事になるので、
	 * ConcurrentModificationException が送出される。<br/>
	 * 問題が解決されるまでは、Ruleで生成されるTaskへ依存性を持つTaskを記述してはならない。
	 * @see http://issues.gradle.org/browse/GRADLE-2023
	 */
	@Test
	void GRADLE_2023() {
		project.plugins.apply(ProjectReportsPlugin)
		project.tasks.taskReport.execute()
	}

}
