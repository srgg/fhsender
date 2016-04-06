package com.github.srgg.aws.fhsender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by srg on 4/6/16.
 */
public final class Utils {
    private static transient ObjectMapper mapper;
    private Utils() {}

    private static ObjectMapper mapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }

        return mapper;
    }

    public static String dumpAsPrettyJson(Object obj) {
        try {
            return mapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{error: 'dumpAsJson("+ obj +") failed.'}";
        }
    }
}
