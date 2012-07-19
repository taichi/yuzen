package org.koshinuke.yuzen.gradle;

import static org.junit.Assert.*;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;
import org.koshinuke.yuzen.TestData

/**
 * @author taichi
 */
class SlideTaskTest {
	Project project

	@Before
	void setUp() {
		this.project = ProjectBuilder.builder().build()
		TestData.overwrite(this.project.ext)
		project.apply plugin: 'yuzen'

		def f = this.project.file("_contents/moge/piro.md")
		f.parentFile.mkdirs()
		f.text = "# testdata\n* aaa\n* bbb\n"

		def src = new File("src/resources/_templates/slide/")
		assert src.exists()
		project.copy {
			from src.toURI().path
			into project.file('_templates/')
		}
	}

	@Test
	void execute() {
		project.tasks.slide.execute()
		def dest = project.file("$project.buildDir/yuzen/moge/piro/index.html")
		assert dest.exists()
		println dest.text
	}
}
