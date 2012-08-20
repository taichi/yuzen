package org.koshinuke.jgit

import javax.annotation.Nonnull

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.dircache.DirCache
import org.eclipse.jgit.lib.Repository

/**
 * @author taichi
 */
class GGitUtil {

	def static handle(@Nonnull Git git, Closure closure) {
		Objects.requireNonNull(git)
		handle(git.getRepository(), closure)
	}

	def static handle(@Nonnull Repository repo, Closure closure) {
		Objects.requireNonNull(repo)
		try {
			closure(repo)
		} finally {
			repo.close()
		}
	}

	def static lockDirCache(@Nonnull Repository repo, Closure closure) {
		Objects.requireNonNull(repo)
		DirCache dc = null
		try {
			dc = repo.lockDirCache()
			closure(dc)
		} finally {
			if(dc != null) {
				dc.unlock()
			}
		}
	}
}
