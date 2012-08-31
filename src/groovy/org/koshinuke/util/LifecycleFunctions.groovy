package org.koshinuke.util

import javax.annotation.Nonnull

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author taichi
 */
class LifecycleFunctions {

	static final Logger LOG = LoggerFactory.getLogger(LifecycleFunctions)

	static def Closure silently = { dispose, value ->
		try {
			dispose(value)
		} catch(Exception e) {
			LOG.warn(e.message, e)
		}
	}

	static def Closure through = { throw it }

	public static <T, R> R handle(@Nonnull Closure<T> factory, @Nonnull Closure dispose, @Nonnull Closure<R> process) {
		Objects.requireNonNull(dispose)
		return handle(factory, silently.curry(dispose), through, process)
	}

	public static <T, R> R handle(@Nonnull Closure<T> factory, @Nonnull Closure dispose, @Nonnull Closure<R> exceptionHandler, @Nonnull Closure<R> process) {
		Objects.requireNonNull(factory)
		Objects.requireNonNull(dispose)
		Objects.requireNonNull(exceptionHandler)
		Objects.requireNonNull(process)
		T target
		try {
			target = factory()
			return process(target)
		} catch(Exception e) {
			return exceptionHandler(e)
		} finally {
			if(target) {
				dispose(target)
			}
		}
	}
}
