package muni.scrutiny.cmdapp.actions.base;

public class ActionException extends Exception {
    public ActionException(String message) {
        super(message);
    }
    public ActionException(Throwable cause) {
        super(cause);
    }
    public ActionException(String message, Throwable cause) {
        super(message, cause);
    }
}
