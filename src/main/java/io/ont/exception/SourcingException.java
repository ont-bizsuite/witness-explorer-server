package io.ont.exception;


public class SourcingException extends RuntimeException {

    private String errDesCN;

    private String errDesEN;

    private int errCode;

    private String action;

    public SourcingException(String msg) {
        super(msg);
    }

    public SourcingException() {
        super();
    }

    public SourcingException(String action, String errDesCN, String errDesEN, int errCode) {
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
