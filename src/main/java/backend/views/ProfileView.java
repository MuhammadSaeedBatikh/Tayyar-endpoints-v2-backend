package backend.views;

import com.google.appengine.api.datastore.GeoPt;

import backend.general.Viewable;
import backend.profiles.Profile;

/**
 * Created by Muhammad on 25/07/2017.
 */

public class ProfileView implements Viewable {
    public Long id;
    public String name;
    public String email;
    public String phone;
    public String imageURl;
    public GeoPt currentLocationGeoPt;
    public String address;

    public ProfileView(Long id, String name, String email,
                       String phone, String imageURl, String address, GeoPt currentLocationGeoPt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.imageURl = imageURl;

        this.address = address;
    }

    public ProfileView(Profile profile) {
        this.id = profile.id;
        this.name = profile.name;
        this.email = profile.email;
        this.phone = profile.phone;
        this.imageURl = profile.imageURl;
        this.address = profile.address;
    }
}
