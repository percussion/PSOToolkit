/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.delivery.s3;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.pso.assembler.PSValidatingVelocityAssembler;
import com.percussion.rx.delivery.IPSDeliveryHandler;
import com.percussion.rx.delivery.IPSDeliveryResult;
import com.percussion.rx.delivery.IPSDeliveryResult.Outcome;
import com.percussion.rx.delivery.PSDeliveryException;
import com.percussion.rx.delivery.data.PSDeliveryResult;
import com.percussion.rx.delivery.impl.PSBaseDeliveryHandler;


/***
 * Provides a delivery handler for publishing content to Amazon's S3
 * service. 
 * 
 * @author natechadwick
 *
 */
public class S3DeliveryHandler extends PSBaseDeliveryHandler implements IPSExtension,IPSDeliveryHandler {

	private AmazonS3 s3;
	private static String ACCESS_KEY_PARAM = "accessKey";
	private static String SECRET_KEY_PARAM = "secretKey";
	private static String TRANSACTIONAL_PARAM = "transactional";
	private static String BUCKET_PARAM = "bucketName";
	
	private String accessKey;
	private String secretKey;
	private Boolean transactional;
	private String bucketName;
	private IPSExtensionDef m_def = null;
	
	private static final Log log = LogFactory.getLog(S3DeliveryHandler.class);
	
	@Override
	protected IPSDeliveryResult doDelivery(Item item, long jobid, String location)
			throws PSDeliveryException {
		
		PutObjectResult por = s3.putObject(new PutObjectRequest(bucketName, location, item.getFile()));
		log.debug("Published " + location + " to bucket " + bucketName + " with MD5 hash " + por.getContentMd5());
		PSDeliveryResult result = new PSDeliveryResult(Outcome.DELIVERED, location, item.getId(), jobid, item.getId().getUUID(),location.getBytes());
		
		return result;
	}

	@Override
	protected IPSDeliveryResult doRemoval(Item item, long jobid, String location) {
			s3.deleteObject(new DeleteObjectRequest(bucketName,location));
			return  new PSDeliveryResult(Outcome.DELIVERED, location, item.getId(), jobid, item.getId().getUUID(),location.getBytes());
	}

	@Override
	public void init(IPSExtensionDef params, File codeRoot)
			throws PSExtensionException {		
		
		log.debug("Initializing Amazon S3 Delivery Handler...");
		m_def = params.clone();
		
		accessKey = params.getInitParameter(ACCESS_KEY_PARAM);
		secretKey = params.getInitParameter(SECRET_KEY_PARAM);
		transactional = Boolean.getBoolean(params.getInitParameter(TRANSACTIONAL_PARAM));
		bucketName = params.getInitParameter(BUCKET_PARAM);
		
		S3ClientOptions opts = new S3ClientOptions();
		opts.setPathStyleAccess(true);
		
		BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);

		s3 = new AmazonS3Client(creds);
		s3.setS3ClientOptions(opts);
	
		if(!s3.doesBucketExist(bucketName))
			throw new PSExtensionException(this.getClass().getName(),"The specified bucket " + bucketName + " does not exist!");
		
	}

	
}
