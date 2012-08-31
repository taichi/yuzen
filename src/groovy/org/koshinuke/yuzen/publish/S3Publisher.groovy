package org.koshinuke.yuzen.publish

import groovy.io.FileType

import java.nio.file.Path

import org.eclipse.jgit.util.StringUtils;
import org.gradle.util.ConfigureUtil
import org.koshinuke.amazonaws.AmazonWebServiceClientUtil
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
import com.amazonaws.services.s3.model.BucketPolicy;
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.Region
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

	def Region region = Region.US_Standard
	def indexDocumentSuffix = 'index.html'
	def errorDocument = 'error.html'

	@Override
	public void publish(File rootDir) {
		if(bucketName) {
			AmazonWebServiceClientUtil.handle(newClient) {
				tryBucket(it)
				transferFiles(it, rootDir)
			}
		} else {
			throw new IllegalStateException("bucketName is null or empty")
		}
	}

	def newClient = {
		if(accessKeyId && secretKey) {
			return new AmazonS3Client(new BasicAWSCredentials(accessKeyId, secretKey), this.config)
		}
		new AmazonS3Client(new AWSCredentialsProviderChain(
				new EnvironmentVariableCredentialsProvider(),
				new SystemPropertiesCredentialsProvider()), this.config)
	}

	def tryBucket(AmazonS3 s3) {
		if(s3.doesBucketExist(bucketName) == false) {
			s3.createBucket(bucketName, region)
		}
		setUpWebsiteConfiguration(s3)
		setUpBucketPolicy(s3)
	}

	/**
	 * @param s3
	 * @return
	 * @see <a href="http://docs.amazonwebservices.com/AmazonS3/latest/dev/WebsiteHosting.html">Hosting Websites on Amazon S3</a>
	 */
	def setUpWebsiteConfiguration(AmazonS3 s3) {
		BucketWebsiteConfiguration web = s3.getBucketWebsiteConfiguration(bucketName)
		if(web == null) {
			BucketWebsiteConfiguration conf = new BucketWebsiteConfiguration(indexDocumentSuffix, errorDocument)
			s3.setBucketWebsiteConfiguration(bucketName, conf)
		} else {
			LOG.info("current setting is \n\tindexDocumentSuffix : $web.indexDocumentSuffix\n\terrorDocument : $web.errorDocument\n")
		}
	}

	/**
	 * @param s3
	 * @return
	 * @see <a href="http://docs.amazonwebservices.com/AmazonS3/latest/dev/UsingBucketPolicies.html">Using Bucket Policies</a>
	 */
	def setUpBucketPolicy(AmazonS3 s3) {
		BucketPolicy bp = s3.getBucketPolicy(bucketName)
		if(StringUtils.isEmptyOrNull(bp.policyText)) {
			// cf. http://docs.amazonwebservices.com/AmazonS3/latest/dev/AccessPolicyLanguage_UseCases_s3_a.html
			def policy = """{
    "Version": "2008-10-17",
    "Statement": [{
        "Sid": "AllowAnonymousRead",
        "Effect": "Allow",
        "Principal": {"AWS": "*"},
        "Action": ["s3:GetObject"],
        "Resource": ["arn:aws:s3:::$bucketName/*"]
    }]
}"""
			s3.setBucketPolicy(bucketName, policy)
		} else {
			LOG.info("current policy is\n$bp.policyText\n")
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
