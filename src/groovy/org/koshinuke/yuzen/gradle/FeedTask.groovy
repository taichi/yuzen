package org.koshinuke.yuzen.gradle

import java.io.File;

import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileVisitor
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil
import org.koshinuke.yuzen.Markers;
import org.pegdown.Extensions
import org.pegdown.PegDownProcessor

import com.sun.syndication.feed.synd.SyndContent
import com.sun.syndication.feed.synd.SyndContentImpl
import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.feed.synd.SyndEntryImpl
import com.sun.syndication.feed.synd.SyndFeed
import com.sun.syndication.feed.synd.SyndFeedImpl
import com.sun.syndication.feed.synd.SyndLink
import com.sun.syndication.feed.synd.SyndLinkImpl
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.feed.synd.SyndPersonImpl
import com.sun.syndication.io.SyndFeedOutput

/**
 * @author taichi
 */
class FeedTask extends ConventionTask {

	@Input
	def feedType

	@Input
	def syndicationURI

	@Input
	def title

	@Input
	def author

	@InputFiles
	ConfigurableFileTree contents

	@OutputFile
	File destinationFile

	def FeedModel model

	FeedTask() {
	}

	@TaskAction
	def makeFeed() {
		def contents = []
		getContents().visit([
					visitDir : {
					},
					visitFile : {
						contents.add new Content(it)
					}
				] as FileVisitor)
		contents.sort { l, r ->
			r.timestamp <=> l.timestamp
		}

		SyndFeed feed = newFeed()
		feed.entries = contents.collect { newEntry(it) }

		File dest = this.getDestinationFile()
		File parent = dest.parentFile
		if(parent.exists() == false && parent.mkdirs() == false) {
			logger.error(Markers.BOUNDARY, "fail to make dir {}", parent)
			return
		}
		SyndFeedOutput output = new SyndFeedOutput()
		dest.withWriter {
			output.output(feed, it)
		}
	}

	def newFeed() {
		SyndFeed feed = new SyndFeedImpl()
		feed.feedType = this.getFeedType()
		feed.title = this.getTitle()
		feed.links = [
			link {
				rel = 'self'
				href = "$syndicationURI/${destinationFile.name}"
			},
			link { href = "$syndicationURI" }
		]
		feed.publishedDate = new Date()
		SyndPerson person = new SyndPersonImpl()
		person.name = this.getAuthor()
		feed.authors = [person]
		feed.modules = []

		this.model.withFeed.execute(feed)
		return feed
	}

	def newEntry(Content c) {
		SyndEntry entry = new SyndEntryImpl()
		entry.title = c.title
		entry.links = [
			link { href = "$syndicationURI/$c.url" }
		]
		entry.publishedDate = c.timestamp
		SyndContent content = new SyndContentImpl()
		content.type = 'text/html'
		PegDownProcessor md = new PegDownProcessor(Extensions.ALL)
		content.value = md.markdownToHtml(c.rawfile.text)
		entry.contents = [content]
		entry.modules = []
		this.model.withEntry.execute(entry)
		return entry
	}

	def link(Closure closure) {
		SyndLink link = new SyndLinkImpl()
		ConfigureUtil.configure(closure, link)
	}
}
