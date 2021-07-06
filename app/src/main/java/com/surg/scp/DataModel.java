package com.surg.scp;

public class DataModel {

    // private variables
    public int _id;


    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getDigitNo() {
        return digitNo;
    }

    public void setDigitNo(String digitNo) {
        this.digitNo = digitNo;
    }

    public String getSoundType() {
        return soundType;
    }

    public void setSoundType(String soundType) {
        this.soundType = soundType;
    }

    public String getTypeNo() {
        return typeNo;
    }

    public void setTypeNo(String typeNo) {
        this.typeNo = typeNo;
    }

    public String devId;
    public String digitNo;
    public String soundType;
    public String typeNo;

    public String getSound_id() {
        return sound_id;
    }

    public void setSound_id(String sound_id) {
        this.sound_id = sound_id;
    }

    public  String sound_id;





    public DataModel() {
        super();
    }

    // getting ID
    public int getID() {
        return this._id;
    }

    // setting id
    public void setID(int id) {
        this._id = id;
    }

}
