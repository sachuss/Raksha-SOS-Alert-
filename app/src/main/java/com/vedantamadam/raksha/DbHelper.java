package com.vedantamadam.raksha;

import android.util.Log;

import com.vedantamadam.raksha.models.EmergencyContact;

import io.realm.Realm;

public class DbHelper {
    public static void db_saveSOSContacts(EmergencyContact contact) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransactionAsync(transactionRealm -> {
                transactionRealm.insert(contact);
            });
            Log.v("raksha", "SOS contact " +contact.getContact_no()+ " saved to DB");
        } catch (Exception ex) {
            Log.e("raksha", ex.getMessage());
        }

    }//eo function

    public static void db_readSOSContacts(EmergencyContact contact) {
//        Realm realm = null;
//        try {
//            realm = Realm.getDefaultInstance();
//            realm.executeTransactionAsync(transactionRealm -> {
//                transactionRealm.insert(contact);
//            });
//            Log.v("raksha", "SOS contact " +contact.getContact_no()+ " saved to DB");
//        } catch (Exception ex) {
//            Log.e("raksha", ex.getMessage());
//        }

    }//eo function
}
