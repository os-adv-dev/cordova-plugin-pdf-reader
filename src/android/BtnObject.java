package org.apache.cordova.pdfpluginmanager;

/**
 * Created by manuelmouta on 19/12/2016.
 */

public class BtnObject {

    private int id;
    private String name;
    private String isDefaulf;
    private String isDisabledUntilEOF;

    public BtnObject(int id, String name, String isDefaulf, String isDisabledUntilEOF) {
        this.id = id;
        this.name = name;
        this.isDefaulf = isDefaulf;
        this.isDisabledUntilEOF = isDisabledUntilEOF;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String isDefaulf() {
        return isDefaulf;
    }

    public String isDisabledUntilEOF() {return isDisabledUntilEOF;}

}
