package org.koshinuke.yuzen.github

import java.nio.file.Files

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.util.FileUtils
import org.koshinuke.jgit.GGitUtil


/**
 * @author taichi
 */
class GhPagesTestingSupport {

	File tmpDir

	File repoDir

	File workingDir

	File rootDir

	def initialize() {
		this.tmpDir = Files.createTempDirectory("ghpages-test").toFile()
		def mkdir = {
			def d = new File(tmpDir,it)
			d.mkdirs()
			return d
		}
		this.repoDir = mkdir("repo")
		this.workingDir = mkdir("working")
		this.rootDir = mkdir("build")

		Git g = Git.init().setDirectory(this.repoDir).call()
		GGitUtil.handle(g) {
			makeFiles(repoDir, ["aaa", "bbb", "ccc","ddd/eee"])

			g.add().addFilepattern(".").call()
			g.commit().setMessage("commit message").call()
		}
	}


	def makeFiles(dir, names) {
		names.each {
			def f = new File(dir, "${it}.txt")
			f.parentFile.mkdirs()
			f.text = "${it} ${new Date()}"
		}
	}

	def dispose() {
		FileUtils.delete(this.tmpDir, FileUtils.RECURSIVE)
	}
}
