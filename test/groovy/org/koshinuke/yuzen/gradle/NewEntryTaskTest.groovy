package org.koshinuke.yuzen.gradle;

import static org.junit.Assert.*;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
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
}
