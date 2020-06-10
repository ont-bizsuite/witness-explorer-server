package io.ont.exception;


public class JWTException extends RuntimeException {

    private String errDesCN;

    private String errDesEN;

    private int errCode;

    private String action;

    public JWTException(String msg) {
        super(msg);
    }

    public JWTException() {
        super();
    }

    public JWTException(String action, String errDesCN, String errDesEN, int errCode) {
        this.action = action;
        this.errDesCN = errDesCN;
        this.errDesEN = errDesEN;
        this.errCode = errCode;
    }

    public String getErrDesCN() {
        return errDesCN;
    }

    public String getErrDesEN() {
        return errDesEN;
    }

    public int getErrCode() {
        return errCode;
    }

    public String getAction() {
        return action;
    }
}
