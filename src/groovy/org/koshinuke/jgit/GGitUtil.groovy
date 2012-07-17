package org.koshinuke.jgit

import javax.annotation.Nonnull;

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository;

/**
 * @author taichi
 */
class GGitUtil {

	public static void handle(@Nonnull Git git, Closure closure) {
		handle(git.getRepository(), closure)
	}

	public static void handle(@Nonnull Repository repo, Closure closure) {
		try {
			closure()
		} finally {
			repo.close()
		}
	}
}
