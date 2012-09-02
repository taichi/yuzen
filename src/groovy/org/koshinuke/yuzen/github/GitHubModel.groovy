package org.koshinuke.yuzen.github

import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.RequestException
import org.eclipse.egit.github.core.service.RepositoryService
import org.eclipse.jgit.util.StringUtils
import org.gradle.api.Project
import org.koshinuke.yuzen.gradle.ProjectUtil


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
		this.username = ProjectUtil.getProperty(project, 'github.username')
		this.password = ProjectUtil.getProperty(project, 'github.password')
	}

	def makeRepositoryService() {
		GitHubClient gc = new GitHubClient()
		gc.setCredentials(this.username, this.password)
		new RepositoryService(gc)
	}

	def getFirstPage(Closure closure) {
		if(this.repos == null) {
			try {
				def sv = makeRepositoryService()
				def list = closure(sv).collect().flatten().sort {
					l , r -> r.updatedAt <=> l.updatedAt
				}
				def s = list.size()
				def end = (recentReposSize < s ?  recentReposSize : s) - 1
				this.repos = list[0..end]
			} catch(RequestException e) {
				this.project.logger.error e.getMessage(), e
				this.repos = []
			}
		}
		return this.repos
	}

	def getRepos() {
		if(StringUtils.isEmptyOrNull(username) || StringUtils.isEmptyOrNull(password)) {
			return []
		}
		return getFirstPage {
			it.pageRepositories(this.reposFilter, this.recentReposSize)
		}
	}

	def getOrgRepos(org) {
		getFirstPage {
			it.pageOrgRepositories(org, this.reposFilter, this.recentReposSize)
		}
	}
}
