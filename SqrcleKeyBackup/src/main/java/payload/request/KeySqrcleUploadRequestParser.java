package payload.request;

import com.fasterxml.jackson.databind.ObjectMapper;

public class KeySqrcleUploadRequestParser {
    public static KeySqrcleUploadRequest parseRequest(String requestBody) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        KeySqrcleUploadRequest keyUploadRequest = objectMapper.readValue(requestBody, KeySqrcleUploadRequest.class);
        return keyUploadRequest;
    }
}
