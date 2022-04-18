package client;

import java.util.ArrayList;

public class ClientProcess {
    private String value;
    private long value_timestamp;
    private long wts;
    private ArrayList<Integer> ackslist;
    private long rid;
    private ArrayList<String> readlist;

    public ClientProcess(){
        value = null;
        value_timestamp = 0;
        wts = 0;
        ackslist = new ArrayList<>();
        rid  = 0;
        readlist = new ArrayList<>();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getValue_timestamp() {
        return value_timestamp;
    }

    public void setValue_timestamp(long value_timestamp) {
        this.value_timestamp = value_timestamp;
    }

    public long getWts() {
        return wts;
    }

    public void setWts(long wts) {
        this.wts = wts;
    }

    public ArrayList<Integer> getAckslist() {
        return ackslist;
    }

    public void setAckslist(ArrayList<Integer> ackslist) {
        this.ackslist = ackslist;
    }

    public long getRid() {
        return rid;
    }

    public void setRid(long rid) {
        this.rid = rid;
    }

    public ArrayList<String> getReadlist() {
        return readlist;
    }

    public void setReadlist(ArrayList<String> readlist) {
        this.readlist = readlist;
    }
}
