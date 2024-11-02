package lk.ijse.SE10_NETWORK_BACKEND.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(String message) {
        super(message);
    }
}