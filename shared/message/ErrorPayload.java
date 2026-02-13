package shared.message;

import java.io.Serializable;

public class ErrorPayload implements Serializable {
    private String code;
    private String message;

    public ErrorPayload(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "Error " + code + ": " + message;
    }
}
