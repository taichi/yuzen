package org.koshinuke.yuzen.github

import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.RepositoryService
import org.gradle.api.Project;


/**
 * @author taichi
 */
class GitHubPluginExtension {

	final Project project

	String username

	String password

	def reposFilter = [type: 'public']

	int recentReposSize = 5

	def repos

	GitHubPluginExtension(Project project) {
		this.project = project
		this.username = project.property('github_username')
		this.password = project.property('github_password')
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
