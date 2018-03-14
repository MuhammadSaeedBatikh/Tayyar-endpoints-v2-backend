package backend.general;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;

import backend.profiles.Profile;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 16/08/2017.
 */
@Entity
@Cache
public class BlackListedProfile {
    @Id
    public Long id;
    @Index
    public Long profileId;
    @Index
    public String wifiMac;
    @Index
    public String deviceMac;
    @Index
    public String email;
    @Index
    public String phone;
    @Index
    public String firebaseUid;
    @Index
    public List<String> regTokens = new ArrayList<>();
    public String whyBlackListed;

    public BlackListedProfile(Profile profile) {
        profileId = profile.id;
        wifiMac = profile.wifiMac;
        deviceMac = profile.deviceMac;
        email = profile.email;
        phone = profile.phone;
        this.firebaseUid = profile.firebaseUid;
        regTokens = profile.getRegTokenList();
    }

    public BlackListedProfile(String phone, String firebaseUid, String regToken) {
        this.phone = phone;
        this.firebaseUid = firebaseUid;
        this.regTokens.add(regToken);
    }

    public static boolean isInBlackList(BlackListedProfile blackListedProfile) {
        Query<BlackListedProfile> query = ofy().load().type(BlackListedProfile.class);
        boolean isIn;
        int count = 0;

        count += query.filter("profileId =", blackListedProfile.profileId).count();
        if (blackListedProfile.wifiMac != null)
            count += query.filter("wifiMac =", blackListedProfile.wifiMac).count();
        if (blackListedProfile.deviceMac != null)
            count += query.filter("deviceMac =", blackListedProfile.deviceMac).count();
        if (blackListedProfile.email != null)
            count += query.filter("email =", blackListedProfile.email).count();
        if (blackListedProfile.phone != null)
            count += query.filter("phone =", blackListedProfile.phone).count();
        if (blackListedProfile.firebaseUid != null)
            count += query.filter("firebaseUid =", blackListedProfile.firebaseUid).count();

        for (String regToken : blackListedProfile.regTokens) {
            if (regToken != null)
                count += query.filter("regTokenList =", regToken).count();
            //// TODO: 08/12/2017  regToken blacklisted bug
        }

        isIn = count > 0;
        return isIn;
    }


    public static boolean isInBlackList(Profile profile) {
        BlackListedProfile blackListedProfile = new BlackListedProfile(profile);
        return isInBlackList(blackListedProfile);
    }

    public static boolean isInBlackList(String phone, String firebaseUid, String regToken) {
        return isInBlackList(new BlackListedProfile(phone, firebaseUid, regToken));
    }

    public static void putInBlackList(Profile profile, String whyBlackListed) {
        if (isInBlackList(profile)) {
            return;
        } else {
            BlackListedProfile blackListedProfile = new BlackListedProfile(profile);
            blackListedProfile.whyBlackListed = whyBlackListed;
            ofy().save().entity(blackListedProfile).now();
            profile.putInBlackList();
        }
    }

    @Override
    public String toString() {
        return "BlackListedProfile{" +
                "id=" + id +
                ", profileId=" + profileId +
                ", wifiMac='" + wifiMac + '\'' +
                ", deviceMac='" + deviceMac + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", firebaseUid='" + firebaseUid + '\'' +
                ", regTokens=" + regTokens +
                ", whyBlackListed='" + whyBlackListed + '\'' +
                '}';
    }
}
