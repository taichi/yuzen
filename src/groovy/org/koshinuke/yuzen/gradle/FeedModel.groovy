package org.koshinuke.yuzen.gradle

import groovy.lang.Closure;


import org.gradle.listener.ActionBroadcast;

import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.feed.synd.SyndFeed

/**
 * @author taichi
 */
class FeedModel {

	def String feedType = 'atom_1.0' // rss_2.0

	def String title

	def String syndicationURI

	def String author

	ActionBroadcast<SyndFeed> withFeed = new ActionBroadcast<>()

	ActionBroadcast<SyndEntry> withEntry = new ActionBroadcast<>()

	def withFeed(Closure closure) {
		this.withFeed.add closure
	}

	def withEntry(Closure closure) {
		this.withEntry.add closure
	}
}
