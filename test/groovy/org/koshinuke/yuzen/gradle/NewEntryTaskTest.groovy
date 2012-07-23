package org.koshinuke.yuzen.gradle;

import static org.junit.Assert.*;

import org.eclipse.jgit.util.FileUtils
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.After
import org.junit.Before;
import org.junit.Test
import org.koshinuke.yuzen.TestData

/**
 * @author taichi
 */
class NewEntryTaskTest {

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
	void newEntry() {
		project.title = "testTitle"
		project.tasks.post.execute()
		def tree = project.fileTree (project.yuzen.contentsDir, { include '**/*.md' })
		def list = new ArrayList<File>(tree.filter { it.isFile() }.files)
		assert 1 == list.size()
		assert "${project.title}.md" == list[0].name
		assert 0 < list[0].text.length()
	}

	@Test
	void newPage() {
		project.pagename = "pages/profile.md"
		project.tasks.page.execute()
		def tree = project.fileTree (project.yuzen.contentsDir, { include '**/*.md' })
		def list = new ArrayList<File>(tree.filter { it.isFile() }.files)
		assert 1 == list.size()
		assert 0 < list[0].text.length()
		assert "profile" == project.tasks.page.getTitle()
		assert "profile.md" == list[0].name
	}
}
