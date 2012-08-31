package org.koshinuke.yuzen.publish;

import static org.junit.Assert.*

import java.security.SecureRandom

import groovy.io.FileType

import org.eclipse.jgit.util.StringUtils
import org.junit.Before
import org.junit.Test
import org.koshinuke.amazonaws.AmazonWebServiceClientUtil
import org.koshinuke.yuzen.TestData

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.BucketPolicy
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration
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
		if(h && p) {
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
		AmazonWebServiceClientUtil.handle(target.newClient, assertUploadedObjects.curry(root))
	}

	def assertUploadedObjects = { File root, AmazonS3 s3 ->
		def keys = []
		try {
			ObjectListing list = s3.listObjects(target.bucketName)
			keys = list.objectSummaries.collect { new DeleteObjectsRequest.KeyVersion(it.key) }

			def counter = 0
			root.traverse type: FileType.FILES, visit: { counter++ }
			assert list.objectSummaries.size() == counter
		} finally {
			if(keys.empty == false) {
				DeleteObjectsRequest delreq = new DeleteObjectsRequest(target.bucketName)
				delreq.keys = keys
				s3.deleteObjects(delreq)
			}
		}
	}

	@Test
	void setUpTest() {
		File root = new File("test/resources/taskconsumer/_contents")
		long l = new SecureRandom().nextLong()
		def bn = 'test-yuzen-' + ((l == Long.MIN_VALUE) ? 0 : Math.abs(l))
		target.bucketName = bn
		target.storageClass = 'REDUCED_REDUNDANCY'
		target.publish(root)
		AmazonWebServiceClientUtil.handle(target.newClient) { AmazonS3 s3 ->
			try {
				assertUploadedObjects(root, s3)
				BucketWebsiteConfiguration web = s3.getBucketWebsiteConfiguration(bn)
				assert web != null
				assert web.indexDocumentSuffix == 'index.html'
				assert web.errorDocument == 'error.html'
				BucketPolicy bp = s3.getBucketPolicy(bn)
				assert StringUtils.isEmptyOrNull(bp.policyText) == false
			} finally {
				s3.deleteBucket(bn)
			}
		}
	}
}
