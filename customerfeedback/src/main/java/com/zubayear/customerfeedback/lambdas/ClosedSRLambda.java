package com.zubayear.customerfeedback.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zubayear.customerfeedback.models.CustomerMessage;
import com.zubayear.customerfeedback.models.SRModel;

import java.util.Arrays;
import java.util.List;

public class ClosedSRLambda {
    private final AmazonS3 amazonS3 = AmazonS3ClientBuilder.defaultClient();
    private final ObjectMapper om = new ObjectMapper();
    private final AmazonSNS amazonSNS = AmazonSNSClientBuilder.defaultClient();
    private final String TOPIC_NAME = System.getenv("TOPIC_NAME");

    public void uploadToS3(final S3Event event, final Context context) {
        event.getRecords().forEach(s3EventNotificationRecord -> {
            S3ObjectInputStream objectContent = amazonS3.getObject(s3EventNotificationRecord.getS3().getBucket().getName(), s3EventNotificationRecord.getS3().getObject().getKey())
                    .getObjectContent();
            try {
                List<SRModel> srLst = Arrays.asList(om.readValue(objectContent, SRModel[].class));
                srLst.forEach(srModel -> {
                    CustomerMessage msg = new CustomerMessage("Dear user, your SR: " + srModel.getSrNum() + " is resolved. Send Y to 12345 if you're satisfied with the resolution N otherwise. Thanks.");
                    try {
                        amazonSNS.publish(TOPIC_NAME, om.writeValueAsString(msg));
                    } catch (JsonProcessingException e) {
                        context.getLogger().log("exception: " + e);
                    }
                });
                context.getLogger().log("srLst: " + srLst);
            } catch (Exception e) {
                context.getLogger().log("exception: " + e);
            }
        });
    }
}
