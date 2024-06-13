package com.nothing.client;


public class ExeResult {
    private int ret;
    private String out;
    public ExeResult(int ret, String out) {
        this.ret = ret;
        this.out = out;
    }

    public int getRet() {
        return ret;
    }

    public String getOut() {
        return out;
    }

    @Override
    public String toString() {
        return "ExeResult{" +
                "ret=" + ret +
                ", out='" + out + '\'' +
                '}';
    }
}
