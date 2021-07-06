package com.surg.scp;

public class SpinnerObject {
    private  String databaseId;
    private String databaseValue;

    public SpinnerObject(String databaseId , String databaseValue ) {
        this.databaseId = databaseId;
        this.databaseValue = databaseValue;
    }

    public String getId () {
        return databaseId;
    }

    public String getValue () {
        return databaseValue;
    }

    @Override
    public String toString () {
        return databaseValue;
    }
}
