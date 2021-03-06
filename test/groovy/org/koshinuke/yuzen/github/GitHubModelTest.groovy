package org.koshinuke.yuzen.github;

import static org.junit.Assert.*;

import org.eclipse.jgit.util.StringUtils;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test
import org.koshinuke.yuzen.TestData

/**
 * @author taichi
 */
class GitHubModelTest {

	Project project

	GitHubModel target

	@Before
	void setUp() {
		this.project = ProjectBuilder.builder().build()
		TestData.overwrite(this.project.ext)
		this.target = project.extensions.create('github', GitHubModel, project)
	}

	@Test
	void props() {
		assert StringUtils.isEmptyOrNull(this.target.username) == false
		assert StringUtils.isEmptyOrNull(this.target.password) == false
		assert StringUtils.isEmptyOrNull(this.project.github.username) == false
		assert StringUtils.isEmptyOrNull(this.project.github.password) == false
	}

	@Test
	void repos() {
		def repos = this.target.repos
		assert repos != null
		assert 0 < repos.size()
		repos.each { println "$it.name $it.htmlUrl" }
	}

	@Test
	void orgRepos() {
		def repos = this.target.getOrgRepos('koshinuke')
		assert repos != null
		assert 0 < repos.size()
		repos.each { println "$it.name $it.htmlUrl" }
	}
}
