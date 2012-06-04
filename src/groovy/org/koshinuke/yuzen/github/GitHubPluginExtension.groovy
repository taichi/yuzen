package org.koshinuke.yuzen.github

import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.RepositoryService
import org.gradle.api.Project;


/**
 * @author taichi
 */
class GitHubPluginExtension {

	final Project project

	String user

	String password

	def reposFilter = [type: 'public']

	GitHubPluginExtension(Project project) {
		this.project = project
		this.user = project.property('github_user')
		this.password = project.property('github_password')
	}

	def makeRepositoryService() {
		GitHubClient gc = new GitHubClient()
		gc.setCredentials(this.user, this.password)
		new RepositoryService(gc)
	}

	def getRepos() {
		makeRepositoryService().getRepositories(this.reposFilter)
	}

	def getOrgRepos(org) {
		makeRepositoryService().getOrgRepositories(org, this.reposFilter)
	}
}
