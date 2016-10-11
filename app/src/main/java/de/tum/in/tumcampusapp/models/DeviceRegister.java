package de.tum.in.tumcampusapp.models;

import android.content.Context;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

import de.tum.in.tumcampusapp.auxiliary.AuthenticationManager;
import de.tum.in.tumcampusapp.exceptions.NoPrivateKey;

public class DeviceRegister {

    private String signature;
    private String date;
    private String rand;
    private String device;
    private String publicKey;
    private ChatMember member = null;

    public DeviceRegister(Context c, String publickey, ChatMember member) throws NoPrivateKey {
        //Create some data
        this.date = (new Date()).toString();
        this.rand = new BigInteger(130, new SecureRandom()).toString(32);
        this.device = AuthenticationManager.getDeviceID(c);
        this.publicKey = publickey;

        //Sign this data for verification
        AuthenticationManager am = new AuthenticationManager(c);
        if (member != null) {
            this.member = member;
            this.signature = am.sign(date + rand + this.device + this.member.getLrzId() + this.member.getId());
        } else {
            this.signature = am.sign(date + rand + this.device);
        }
    }

}
