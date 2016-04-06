package com.github.srgg.aws.fhsender;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfilesConfigFile;

import java.io.File;

/**
 * https://github.com/aws/aws-sdk-java/blob/master/src/samples/AmazonKinesisFirehose/AmazonKinesisFirehoseToRedshiftSample.java
 */
public class Main {
    private final static EventSender sender = new EventSender("gen1");

    public static void main(String[] args) throws Exception {
        /*
         * ProfileCredentialsProvider loads AWS security credentials from a
         * .aws/config file in your home directory.
         *
         * These same credentials are used when working with other AWS SDKs and the AWS CLI.
         *
         * You can find more information on the AWS profiles config file here:
         * http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html
         */
        File configFile = new File(System.getProperty("user.home"), ".aws/credentials");
        AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(
                new ProfilesConfigFile(configFile), "default");

        if (credentialsProvider.getCredentials() == null) {
            throw new RuntimeException("No AWS security credentials found:\n"
                    + "Make sure you've configured your credentials in: " + configFile.getAbsolutePath() + "\n"
                    + "For more information on configuring your credentials, see "
                    + "http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html");
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                sender.stop();
            }
        });

        sender.configure(credentialsProvider, "fheventgen-test");
        sender.start();
    }
}
