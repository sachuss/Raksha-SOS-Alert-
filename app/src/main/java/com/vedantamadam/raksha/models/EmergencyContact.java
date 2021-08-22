package com.vedantamadam.raksha.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class EmergencyContact extends RealmObject {
    private String contact_no;
    private String name;
    public EmergencyContact(String name, String contact_no) {
        this.name = name;
        this.contact_no = contact_no;
    }
    public EmergencyContact(){} // RealmObject subclasses must provide an empty constructor
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContact_no() { return contact_no; }
    public void setContact_no(String contact_no) { this.contact_no = contact_no; }

}
