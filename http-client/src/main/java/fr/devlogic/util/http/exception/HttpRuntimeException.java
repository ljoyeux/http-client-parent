package fr.devlogic.util.http.exception;

public class HttpRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -4219168852585053451L;

    private Integer status;
    private String content;

    public HttpRuntimeException(String message) {
        super(message);
    }

    public HttpRuntimeException(Throwable cause) {
        super(cause);
    }

    public HttpRuntimeException(int status, String content) {
        this.status = status;
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "HttpRuntimeException{" +
                "status=" + status +
                ", content='" + content + '\'' +
                '}';
    }
}
