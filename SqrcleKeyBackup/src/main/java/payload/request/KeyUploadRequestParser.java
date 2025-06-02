package payload.request;

import com.fasterxml.jackson.databind.ObjectMapper;

public class KeyUploadRequestParser {
    public static KeyUploadRequest parseRequest(String requestBody) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        KeyUploadRequest keyUploadRequest = objectMapper.readValue(requestBody, KeyUploadRequest.class);
        return keyUploadRequest;
    }
}
