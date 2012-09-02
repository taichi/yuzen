package org.koshinuke.yuzen.publish

import org.eclipse.jgit.util.StringUtils
import org.gradle.api.Project
import org.gradle.util.ConfigureUtil
import org.koshinuke.yuzen.github.GitHubPagesPublisher
import org.koshinuke.yuzen.gradle.ProjectUtil




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
		FTPSPublisher ftps = configure(new FTPSPublisher(), c)
		def u = ProjectUtil.getProperty(project, 'ftps.username')
		def p = ProjectUtil.getProperty(project, 'ftps.password')
		if(StringUtils.isEmptyOrNull(ftps.username) && StringUtils.isEmptyOrNull(ftps.password) && u && p) {
			ftps.username = u
			ftps.password = p
		}
		return ftps
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

	@Override
	public <T extends Publisher> T s3(Map<String, ?> args) {
		return configureS3(byMap(args))
	}

	@Override
	public <T extends Publisher> T s3(Closure<T> configureClosure) {
		return configureS3(by(configureClosure))
	}

	def configureS3(Closure c) {
		def s3 = configure(new S3Publisher(), c)
		def chost = s3.config.proxyHost
		def cport = s3.config.proxyPort
		def host = System.props['http.proxyHost']
		def port = System.props['http.proxyPort']
		if(StringUtils.isEmptyOrNull(chost) && host && port) {
			s3.config {
				proxyHost = host
				proxyPort = port as int
			}
		}
		return s3
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
