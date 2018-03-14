package backend.general;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.response.UnauthorizedException;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.List;

import backend.TestEntity;
import backend.helpers.OfyHelper;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 08/02/2018.
 */

@Entity
@Cache
public class UserPrivileges {
    @Id
    public Long id;
    @Index
    public String role;
    @Index
    public String token; //authDomain:userId

    //default constructor for Entity initialization
    public UserPrivileges() {
    }
    //============


    public UserPrivileges(String role, String token) {
        this.role = role;
        this.token = token;
    }

    /**
     * roles/
     * owner: can create admins
     * admin: can't create admins, but can create drivers can create drivers
     * drivers
     * none
     */
    public static boolean isAdmin(User user) throws UnauthorizedException {
        TestEntity l = new TestEntity();
        String token = generateToken(user);
        l.log("token= "+token);
        List<UserPrivileges> users = ofy().load().type(UserPrivileges.class)
                .filter("token =", token)
                .list();
        l.log("users "+users);
        if (users.size() == 0) {
            l.log("nothing has been returned");
            throw new UnauthorizedException("none shall pass");
        }

        UserPrivileges userPrivileges = users.get(0);
        if (!userPrivileges.role.equals("admin") & !userPrivileges.role.equals("owner")) {
            l.log("condition1 "+!userPrivileges.role.equals("admin"));
            l.log("condition1 "+!userPrivileges.role.equals("owner"));
            l.log("failed to check");
            throw new UnauthorizedException("none shall pass");
        }
        return true;
    }

    public static void createFirstOwner(User user) {
        String token = generateToken(user);
        UserPrivileges userPrivileges = new UserPrivileges("owner", token);
        userPrivileges.save();
    }


    public void save() {
        ofy().save().entity(this).now();
    }

    public static String generateToken(User user) {
        return user.getEmail() + ":" + user.getId();
    }

    @Override
    public String toString() {
        return "UserPrivileges{" +
                "id=" + id +
                ", role='" + role + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
