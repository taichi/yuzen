package org.koshinuke.yuzen.publish

import org.eclipse.jgit.util.StringUtils;
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.koshinuke.yuzen.github.GitHubPagesPublisher




/**
 * @author taichi
 */
class DefaultPublisherHandler implements PublisherHandler {

	def Project project
	def List publishers

	protected DefaultPublisherHandler(project, publishers) {
		this.project = project
		this.publishers = publishers
	}

	@Override
	public <T extends Publisher> T ftps(Map<String, ?> args) {
		return configureFtps(byMap(args))
	}

	@Override
	public <T extends Publisher> T ftps(Closure<T> configureClosure) {
		return configureFtps(by(configureClosure))
	}

	def configureFtps(Closure c) {
		return configure(new FTPSPublisher(), c)
	}

	@Override
	public <T extends Publisher> T ghpages(Map<String, ?> args) {
		return configureGhpages(byMap(args))
	}

	@Override
	public <T extends Publisher> T ghpages(Closure<T> configureClosure) {
		return configureGhpages(by(configureClosure))
	}

	def configureGhpages(Closure c) {
		def ghpages = configure(new GitHubPagesPublisher(), c)
		if(ghpages.workingDir == null) {
			ghpages.workingDir = new File(project.getBuildDir(), "tmp/ghpages")
		}
		if(StringUtils.isEmptyOrNull(ghpages.username)) {
			ghpages.username = project.github.username
		}
		if(StringUtils.isEmptyOrNull(ghpages.password)) {
			ghpages.password = project.github.password
		}
		return ghpages
	}


	def byMap(args) {
		return {
			ConfigureUtil.configureByMap(args, it)
		}
	}

	def by(configureClosure) {
		return {
			ConfigureUtil.configure(configureClosure, it)
		}
	}

	private def configure(model, closure) {
		this.publishers.add closure(model)
		return model
	}
}
