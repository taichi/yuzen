package org.koshinuke.amazonaws

import javax.annotation.Nonnull

import org.koshinuke.util.LifecycleFunctions;

import com.amazonaws.AmazonWebServiceClient





/**
 * @author taichi
 */
class AmazonWebServiceClientUtil {

	public static <T extends AmazonWebServiceClient, R> R handle(@Nonnull Closure<T> factory, @Nonnull Closure<R> closure) {
		return LifecycleFunctions.handle(factory, { it.shutdown() }, closure)
	}
}
