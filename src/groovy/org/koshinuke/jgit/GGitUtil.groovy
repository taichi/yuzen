package org.koshinuke.jgit

import javax.annotation.Nonnull

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.koshinuke.util.LifecycleFunctions

/**
 * @author taichi
 */
class GGitUtil {

	public static <R> R handle(@Nonnull Git git, @Nonnull Closure<R> closure) {
		Objects.requireNonNull(git)
		return handle(git.getRepository(), closure)
	}

	public static <R> R handle(@Nonnull Repository repo, @Nonnull Closure<R> closure) {
		Objects.requireNonNull(repo)
		return LifecycleFunctions.handle({repo}, {it.close()}, closure)
	}

	public static <R> R lockDirCache(@Nonnull Repository repo, @Nonnull Closure<R> closure) {
		Objects.requireNonNull(repo)
		return LifecycleFunctions.handle({ repo.lockDirCache() }, { it.unlock() }, closure)
	}
}
