package org.koshinuke.yuzen.publish;

import groovy.lang.Closure;

import java.util.Map;

/**
 * @author taichi
 */
public interface PublisherHandler {

	/**
	 * <p>
	 * Examples:
	 * 
	 * <pre>
	 * yuzen.publish {
	 *     ftps host: 'example.com', username: 'john', password: 'p@55worD', dirprefix: 'foo/bar/baz/'
	 * }
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param args
	 * @return
	 */
	<T extends Publisher> T ftps(Map<String, ?> args);

	/**
	 * <p>
	 * Examples:
	 * 
	 * <pre>
	 * yuzen.publish {
	 *     ftps {
	 *         host = 'example.com'
	 *         username = 'john'
	 *         password = 'p@55worD'
	 *         dirprefix = 'foo/bar/baz/'
	 *     }
	 * }
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param args
	 * @return
	 */
	<T extends Publisher> T ftps(Closure<T> configureClosure);

	/**
	 * <p>
	 * Examples:
	 * 
	 * <pre>
	 * yuzen.publish {
	 *     ghpages repoURI: 'git@github.com:example/example.git', username: 'john', password: 'p@55worD', updateMessage: "Updated at ${new Date()}"
	 * }
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param args
	 * @return
	 */
	<T extends Publisher> T ghpages(Map<String, ?> args);

	/**
	 * <p>
	 * Examples:
	 * 
	 * <pre>
	 * yuzen.publish {
	 *     ghpages {
	 *         repoURI = 'https://github.com/example/example.git'
	 *         username = 'john'
	 *         password = 'p@55worD'
	 *         updateMessage = "Updated at ${new Date()}"
	 *     }
	 * }
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param args
	 * @return
	 */
	<T extends Publisher> T ghpages(Closure<T> configureClosure);
}
