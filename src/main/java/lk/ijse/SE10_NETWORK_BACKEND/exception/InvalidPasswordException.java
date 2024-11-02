package lk.ijse.SE10_NETWORK_BACKEND.exception;

public class InvalidPasswordException extends RuntimeException{
    public InvalidPasswordException(String message) {
        super(message);
    }
}
