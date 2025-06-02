package payload.request;

import com.fasterxml.jackson.databind.ObjectMapper;

public class KeyDownloadRequestParser {
    public static KeyDownloadRequest parseRequest(String requestBody) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        KeyDownloadRequest keyDownloadRequest = objectMapper.readValue(requestBody, KeyDownloadRequest.class);
        return keyDownloadRequest;

    }
}
