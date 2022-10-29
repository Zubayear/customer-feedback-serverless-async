package com.zubayear.customerfeedback.lambdas;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zubayear.customerfeedback.models.CustomerMessage;

public class CustomerMsgLambda {
    private final ObjectMapper om = new ObjectMapper();

    public void saveMsg(final SNSEvent event, final Context context) {
        event.getRecords().forEach(snsRecord -> {
            try {
                CustomerMessage customerMessage = om.readValue(snsRecord.getSNS().getMessage(), CustomerMessage.class);
                context.getLogger().log("customerMessage: " + customerMessage);
                // here we would save the msg to db
            } catch (JsonProcessingException e) {
                context.getLogger().log("exception CustomerMsgLambda.saveMsg: " + e);
            }
        });
    }
}
