package org.koshinuke.jgit

import javax.annotation.Nonnull;

import org.eclipse.jgit.api.Git

/**
 * @author taichi
 */
class GGitUtil {

	public static void handle(@Nonnull Git git, Closure closure) {
		try {
			closure()
		} finally {
			git.getRepository().close()
		}
	}
}
