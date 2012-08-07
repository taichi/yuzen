package org.koshinuke.yuzen.github

import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.RepositoryService
import org.gradle.api.Project


/**
 * @author taichi
 */
class GitHubModel {

	final Project project

	def String projectURI

	String username

	String password

	def reposFilter = [type: 'public']

	int recentReposSize = 5

	def repos

	GitHubModel(Project project) {
		this.project = project
		this.username = prop('github.username')
		this.password = prop('github.password')
	}

	def prop(key) {
		if(project.hasProperty(key)) {
			return project.property(key)
		}
		return null
	}

	def makeRepositoryService() {
		GitHubClient gc = new GitHubClient()
		gc.setCredentials(this.username, this.password)
		new RepositoryService(gc)
	}

	def getFirstPage(Closure closure) {
		if(this.repos == null) {
			def sv = makeRepositoryService()
			def itr = closure(sv)
			if(itr.hasNext()) {
				this.repos = itr.next()
			}
		}
		return this.repos
	}

	def getRepos() {
		getFirstPage {
			it.pageRepositories(this.reposFilter, this.recentReposSize)
		}
	}

	def getOrgRepos(org) {
		getFirstPage {
			it.pageOrgRepositories(org, this.reposFilter, this.recentReposSize)
		}
	}
}
