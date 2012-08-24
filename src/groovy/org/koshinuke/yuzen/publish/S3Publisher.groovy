package org.koshinuke.yuzen.publish



import groovy.io.FileType

import java.nio.file.Path

import org.apache.http.client.CredentialsProvider
import org.gradle.util.ConfigureUtil
import org.koshinuke.yuzen.util.FileUtil

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.SystemPropertiesCredentialsProvider
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.StorageClass

/**
 * @author taichi
 */
class S3Publisher implements Publisher {

	def bucketName = ""
	def dirPrefix = ""

	def List<CredentialsProvider> credentials = []
	def ClientConfiguration config = new ClientConfiguration()
	def accessKeyId, secretKey
	def storageClass = StorageClass.Standard

	@Override
	public void publish(File rootDir) {
		AmazonS3 s3
		try {
			s3 = newClient()
			tryBucket(s3)
			transferFiles(s3, rootDir)
		} finally {
			if(s3 != null) {
				s3.shutdown()
			}
		}
	}

	protected AmazonS3 newClient() {
		if(accessKeyId && secretKey) {
			credentials.add new BasicAWSCredentials(accessKeyId, secretKey)
		}

		def cred = null
		if(this.credentials.isEmpty()) {
			cred = new SystemPropertiesCredentialsProvider()
		} else {
			cred = new AWSCredentialsProviderChain(credentials as CredentialsProvider[])
		}
		return new AmazonS3Client(cred, this.config)
	}

	def tryBucket(AmazonS3 s3) {
		// make bucket if doesn't exist
		// setUp bucket for static web hosting
		// setUp bucket acl for publish
		// log endpoint URL
		BucketWebsiteConfiguration web = s3.getBucketWebsiteConfiguration(bucketName)
		if(web == null) {
			// make new bucket
		} else {
		}
	}

	def transferFiles(AmazonS3 s3, File rootDir) {
		final Path root = rootDir.toPath().toAbsolutePath()
		def concatPath = { dir ->
			def p = FileUtil.slashify(root.relativize(dir))
			"${dirPrefix}${p}"
		}

		rootDir.traverse type: FileType.FILES, visit: {
			def path = concatPath it.toPath()
			s3.putObject(new PutObjectRequest(bucketName, path, it).withStorageClass(storageClass))
		}
	}

	def config(Closure configureClosure) {
		ConfigureUtil.configure(configureClosure, config)
	}
}
