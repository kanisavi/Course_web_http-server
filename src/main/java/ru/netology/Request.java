package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private final String method;
    private final String path;
    private final String queryString;
    private final Map<String, String> headers;
    private final String body;
    private final List<NameValuePair> queryParams;

    public Request(String method, String path, String queryString,
                   Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.queryString = queryString;
        this.headers = headers;
        this.body = body;
        this.queryParams = queryString != null && !queryString.isEmpty()
                ? URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8)
                : Collections.emptyList();
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public String getQueryString() { return queryString; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }

    public String getQueryParam(String name) {
        for (NameValuePair param : queryParams) {
            if (param.getName().equals(name)) {
                return param.getValue();
            }
        }
        return null;
    }

    public Map<String, String> getQueryParams() {
        Map<String, String> result = new LinkedHashMap<>();
        for (NameValuePair param : queryParams) {
            result.put(param.getName(), param.getValue());
        }
        return result;
    }
}
