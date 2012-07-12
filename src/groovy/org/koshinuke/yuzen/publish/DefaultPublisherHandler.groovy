package org.koshinuke.yuzen.publish

import groovy.lang.Closure

import java.util.Map;

import org.gradle.util.ConfigureUtil;

/**
 * @author taichi
 */
class DefaultPublisherHandler implements PublisherHandler {

	def List publishers

	protected DefaultPublisherHandler(publishers) {
		this.publishers = publishers
	}

	@Override
	public <T extends Publisher> T ftps(Map<String, ?> args) {
		return configureFtps {
			ConfigureUtil.configureByMap(args, it)
		}
	}

	def configureFtps(Closure c) {
		def ftps = new FTPSPublisher()
		publishers.add c(ftps)
		return ftps
	}

	@Override
	public <T extends Publisher> T ftps(Closure<T> configureClosure) {
		return configureFtps {
			ConfigureUtil.configure(configureClosure, it)
		}
	}
}
