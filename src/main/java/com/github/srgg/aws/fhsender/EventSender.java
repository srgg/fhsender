package com.github.srgg.aws.fhsender;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseAsyncClient;
import com.amazonaws.services.kinesisfirehose.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Dumb event sender
 */
public class EventSender {
    private final static Logger logger = LoggerFactory.getLogger(EventSender.class);

    // https://github.com/awslabs/aws-akka-firehose
    private final String id;

    private ObjectMapper mapper = new ObjectMapper()
            // For debugging purposes set Timestamp serialization to produce strings in ISO format
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private AmazonKinesisFirehoseAsyncClient firehoseClient;
    private AWSCredentialsProvider provider;
    protected String deliveryStreamName;

    public EventSender(String id) {
        this.id = id;
    }

    public void configure(AWSCredentialsProvider provider, String deliveryStreamName) {
        this.provider = provider;
        this.deliveryStreamName = deliveryStreamName;
    }

    public void start() throws Exception {
        if (isActive()) {
            throw new IllegalStateException("Sender already started");
        }

        if (provider == null) {
            throw new IllegalStateException("Sender failed to start, it is not properly configured.");
        }

        firehoseClient = new AmazonKinesisFirehoseAsyncClient(provider);
        firehoseClient.setRegion(RegionUtils.getRegion("us-west-2"));

        final DescribeDeliveryStreamRequest request = new DescribeDeliveryStreamRequest();
        request.setDeliveryStreamName(deliveryStreamName);

        final String deliveryStreamInfo;
        try {
            final DescribeDeliveryStreamResult response = firehoseClient.describeDeliveryStream(request);
            deliveryStreamInfo = Utils.dumpAsPrettyJson(response.getDeliveryStreamDescription());
        } catch (ResourceNotFoundException ex) {
            doStop();
            throw new Exception("Sender failed to start, delivery stream is not found", ex);
        }

        try {
            sendEvent("Sender started");
        } catch (Exception ex) {
            logger.warn("Can't send start notification", ex);
        }

        logger.info("Sender({}) -- started\n{}", this.id, deliveryStreamInfo);
    }

    private void doStop() {
        if (firehoseClient != null) {
            firehoseClient.shutdown();
            firehoseClient = null;
        }
    }

    public void stop() {
        try {
            sendEvent("Sender stopped");
        } catch (Exception ex) {
            logger.warn("Can't send stop notification", ex);
        }
        doStop();
        logger.info("Sender({}) -- stopped", this.id);
    }

    public boolean isActive() {
        return firehoseClient != null;
    }

    public void sendEvent(Event e) throws Exception {
        e.setSender(this.id);

        final String serialized;
        try {
            serialized = mapper.writeValueAsString(e);
        } catch (JsonProcessingException ex) {
            throw new Exception("Send failed, can't serialize event to json.", ex);
        }

        final PutRecordRequest putRecordRequest = new PutRecordRequest();
        putRecordRequest.setDeliveryStreamName(deliveryStreamName);

        final Record record = new Record();
        record.setData(ByteBuffer.wrap(serialized.getBytes()));

        putRecordRequest.setRecord(record);

        final PutRecordResult result = firehoseClient.putRecord(putRecordRequest);
        logger.trace("Event was generated, recordId: '{}'", result.getRecordId());
    }

    // http://docs.aws.amazon.com/firehose/latest/dev/writing-with-sdk.html
    public void sendEvent(String text) throws Exception {

        final Event e = new Event();
        e.setSender(this.id);
        e.setData(text);

        sendEvent(e);
    }
}
