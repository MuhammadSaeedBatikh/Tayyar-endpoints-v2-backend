package backend.profiles;

import backend.reviews.Review;
import backend.stats.ReviewStats;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 25/07/2017.
 */
@Entity
public abstract class Profile {
    @Id
    public Long id;
    @Index
    public boolean isInBlackList;
    @Index
    public String firebaseUid;
    @Index
    public String name;

    @Index
    public String email;

    @Index
    public String phone;
    public String wifiMac;
    public String deviceMac;
    public String imageURl;
    @Index
    public List<String> regTokenList = new ArrayList<>();

    public List<Long> reviews = new ArrayList<>();
    public ReviewStats reviewStats = new ReviewStats();
    @Index
    public String address;
    @Index
    public Long areaId;
    @Index
    public Long cityId;
    @Index
    public Date creationDate;

    {
        this.creationDate = new Date();
    }

    //default constructor for Entity initialization
    public Profile() {
    }
    //============

    public void saveProfile() {
        ofy().save().entity(this).now();
    }
    //to be used with signUp, updating profile info etc

    public static Profile getProfileByID(Long id) {
        return ofy().load().type(Profile.class).id(id).now();
    }


    public Profile(String name, String firebaseUid, String phone) {
        this.name = name;
        this.firebaseUid = firebaseUid;
        this.phone = phone;
    }

    public void addRegToken(String regToken) {

        if (!this.getRegTokenList().contains(regToken)) {
            this.regTokenList.add(regToken);
            saveProfile();
        }
    }

    public void removeRegToken(String regToken) {
        this.regTokenList.remove(regToken);
        saveProfile();
    }

    public List<String> getRegTokenList() {
        return this.regTokenList;
    }

    public static List<Profile> getByPhone(String phone) {
        return ofy().load().type(Profile.class).filter("phone =", phone).list();
    }

    public static Profile getProfileByFirebaseUid(String firebaseUid) {
        List<Profile> profiles = ofy().load().type(Profile.class).filter("firebaseUid =", firebaseUid).list();
        if (profiles == null) return null;
        else
            return ofy().load().type(Profile.class).filter("firebaseUid =", firebaseUid).list().get(0);
    }

    public static Long getProfileIdByFirebaseUid(String firebaseUid) {
        Profile profile = getProfileByFirebaseUid(firebaseUid);
        if (profile == null)
            return null;
        else {
            return profile.id;
        }
    }

    public static boolean doesProfileExist(Long profileId) {
        return getProfileByID(profileId) != null;
    }

    public void putInBlackList() {
        isInBlackList = true;
        saveProfile();
    }

    public boolean isInBlackList() {
        return isInBlackList;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public void reviewedSomething(Long reviewId) {
        Review review = Review.getByID(reviewId);
        reviews.add(reviewId);
        this.reviewStats.updateWithRating(review.rating);
        saveProfile();
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", firebaseUid='" + firebaseUid + '\'' +
                ", name='" + name + '\'' +
                ", regTokenList=" + regTokenList +
                ", cityId=" + cityId +
                ", creationDate=" + creationDate +
                '}';
    }
}
