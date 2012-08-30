package org.koshinuke.yuzen.publish

import groovy.io.FileType

import java.nio.file.Path

import org.gradle.util.ConfigureUtil
import org.koshinuke.yuzen.util.FileUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
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

	static final Logger LOG = LoggerFactory.getLogger(S3Publisher)

	def bucketName = ""
	def dirPrefix = ""

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

	def AmazonS3 newClient() {
		if(accessKeyId && secretKey) {
			return new AmazonS3Client(new BasicAWSCredentials(accessKeyId, secretKey), this.config)
		}
		new AmazonS3Client(new AWSCredentialsProviderChain(
				new EnvironmentVariableCredentialsProvider(),
				new SystemPropertiesCredentialsProvider()), this.config)
	}

	def tryBucket(AmazonS3 s3) {
		// TODO make bucket if doesn't exist ?
		// Region, Acl...
		// setUp bucket for static web hosting
		// setUp bucket acl for publish
		// log endpoint URL
		BucketWebsiteConfiguration web = s3.getBucketWebsiteConfiguration(bucketName)
		if(web == null) {
			throw new IllegalStateException("$bucketName is not exist or not configured for static web hosting.")
		}
	}

	def transferFiles(AmazonS3 s3, File rootDir) {
		LOG.debug("transferFiles bucket : $bucketName, dir : $rootDir")
		final Path root = rootDir.toPath()
		def concatPath = { dir ->
			def p = FileUtil.slashify(root.relativize(dir))
			"${dirPrefix}${p}"
		}

		rootDir.traverse type: FileType.FILES, visit: {
			LOG.debug("from $it")
			def path = concatPath it.toPath()
			LOG.debug("to $path with $storageClass")
			s3.putObject(new PutObjectRequest(bucketName, path, it).withStorageClass(storageClass))
		}
		LOG.debug("end transfer")
	}

	def config(Closure configureClosure) {
		ConfigureUtil.configure(configureClosure, config)
	}

	def config(Map properties) {
		ConfigureUtil.configureByMap(properties, config)
	}
}
