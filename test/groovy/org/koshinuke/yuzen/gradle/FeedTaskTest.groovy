package org.koshinuke.yuzen.gradle;

import static org.junit.Assert.*

import org.eclipse.jgit.util.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koshinuke.yuzen.TestData

/**
 * @author taichi
 */
class FeedTaskTest {
	Project project

	@Before
	void setUp() {
		this.project = ProjectBuilder.builder().build()

		TestData.overwrite(this.project.ext)

		this.project.apply plugin: 'yuzen'
	}

	@After
	void tearDown() {
		FileUtils.delete(this.project.getProjectDir(), FileUtils.RECURSIVE)
	}

	@Test
	void testMembers() {
		assert project.blog.feed.syndicationURI == null
		project.blog.feed.syndicationURI = 'http://example.com'
		assert project.tasks.blogFeed.syndicationURI != null
	}

	@Test
	void makeFeed() {
		def today = new Date()
		5.times {
			def f = this.project.file("_contents/entry/hoehoe${it}.md")
			f.parentFile.mkdirs()
			f.text = "# aaaaa$it \n* testdata\n* testtest$it"
			f.lastModified = today.minus(3 * it).time
		}
		FeedTask feed = project.tasks.blogFeed
		feed.with {
			feedType = 'atom_1.0'
			syndicationURI = 'http://example.com'
			title = 'TTTT'
			author = 'tester'
		}
		feed.execute()

		assert feed.getDestinationFile().exists()
		def xml = new XmlParser().parse(feed.getDestinationFile())
		assert feed.title == xml.title.text()
		assert feed.author == xml.author.name.text()
		assert xml.entry.size() == 5
	}

	@Test
	void entityResolve() {
		def f = this.project.file("_contents/entry/aaa.md")
		f.parentFile.mkdirs()
		f.text = "if you don't have gradle,"
		FeedTask feed = project.tasks.blogFeed
		feed.with {
			feedType = 'atom_1.0'
			syndicationURI = 'http://example.com'
			title = 'TTTT'
			author = 'tester'
		}
		feed.execute()
		assert feed.getDestinationFile().exists()
	}
}
