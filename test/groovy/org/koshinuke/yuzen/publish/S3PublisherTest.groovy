package org.koshinuke.yuzen.publish;

import static org.junit.Assert.*
import groovy.io.FileType

import org.eclipse.jgit.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.koshinuke.yuzen.TestData

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.DeleteObjectsRequest
import com.amazonaws.services.s3.model.ObjectListing

/**
 * @author taichi
 */
class S3PublisherTest {

	S3Publisher target

	@Before
	public void setUp() {
		TestData.overwrite([:])
		target = new S3Publisher()
		target.bucketName = 'yuzen-test'
		def h = System.properties['http.proxyHost']
		def p = System.properties['http.proxyPort']
		if(StringUtils.isEmptyOrNull(h) == false &&
			StringUtils.isEmptyOrNull(p) == false) {
			target.config {
				proxyHost = h
				proxyPort = p as int
			}
		}
	}

	@Test
	void configureMap() {
		target.config maxConnections: 10, userAgent: 'yuzen'

		assert target.config.maxConnections == 10
		assert target.config.userAgent == 'yuzen'
	}

	@Test
	void test() {
		File root = new File("test/resources/taskconsumer/_contents")
		target.storageClass = 'REDUCED_REDUNDANCY'
		target.publish(root)
		AmazonS3 s3 = target.newClient()
		def keys = []
		try {
			ObjectListing list = s3.listObjects(target.bucketName)

			def counter = 0
			root.traverse type: FileType.FILES, visit: { counter++ }

			assert list.objectSummaries.size() == counter

			keys = list.objectSummaries.collect { new DeleteObjectsRequest.KeyVersion(it.key) }
		} finally {
			if(keys.empty == false) {
				DeleteObjectsRequest delreq = new DeleteObjectsRequest(target.bucketName)
				delreq.keys = keys
				s3.deleteObjects(delreq)
			}
		}
	}
}
