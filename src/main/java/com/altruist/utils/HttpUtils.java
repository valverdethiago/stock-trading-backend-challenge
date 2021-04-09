package com.altruist.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.*;
import java.util.*;

@Slf4j
public class HttpUtils {

    public static URI buildEntityUrl(HttpServletRequest httpServletRequest, UUID accountId) {
        try {
            return new URI(httpServletRequest.getRequestURL() + "/" + accountId.toString());
        } catch (URISyntaxException e) {
            log.warn("Error generating url for {}", accountId);
            return null;
        }
    }
}
