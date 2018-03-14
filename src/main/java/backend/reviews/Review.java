package backend.reviews;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import backend.profiles.Profile;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * User: YourPc
 * Date: 3/14/2017
 */

@Entity
@Cache
public class Review {
    @Id
    public Long id;

    @Index
    public String reviewType; //flag

    @Index
    public Long reviewerID;

    @Index
    public Long revieweeID;

    public String reviewerName = "Anonymous";

    public String comment;

    @Index
    public int rating;

    public int likesCount;
    public List<Long> likedByIds = new ArrayList<>();
    public int dislikesCount;
    public List<Long> dislikedByIds = new ArrayList<>();
    public int helpfulCount;
    public List<Long> markedHelpfulByIds = new ArrayList<>();

    @Index
    public Date creationDate;

    //default constructor for Entity initalization
    public Review() {
    }

    public Review(String reviewType, Long reviewerID, Long revieweeID, boolean anonymous, String comment, int rating) {
        this.reviewType = reviewType;
        this.reviewerID = reviewerID;
        this.revieweeID = revieweeID;
        this.comment = comment;
        this.rating = rating;
        if (!anonymous)
            this.reviewerName = Profile.getProfileByID(reviewerID).name;

        this.creationDate = new Date();
    }

    public static Review getByID(Long id) {
        return ofy().load().type(Review.class).id(id).now();

    }

    public void save() {
        ofy().save().entity(this).now();
    }

    public static Review submitReview(String reviewType, Long reviewerID, Long revieweeID, boolean anonymous, String comment, int rating) {
        Review review = new Review(reviewType, reviewerID, revieweeID, anonymous, comment, rating);
        review.save();
        return review;
    }

    public void gotLiked(Long customerId) {
        if (dislikedByIds.contains(customerId)) {
            this.dislikesCount--;
            this.dislikedByIds.remove(customerId);
        }
        if (this.likedByIds.contains(customerId)) {
            return;
        }
        this.likedByIds.add(customerId);
        this.likesCount++;
        save();
    }

    public void gotDisliked(Long customerId) {
        if (this.likedByIds.contains(customerId)) {
            this.likesCount--;
            this.likedByIds.remove(customerId);
        }
        if (this.dislikedByIds.contains(customerId)) {
            return;
        }
        this.dislikedByIds.add(customerId);
        this.dislikesCount++;
        save();
    }

    public void markedAsHelpful(Long customerId) {
        if (!this.markedHelpfulByIds.contains(customerId)) {
            this.helpfulCount++;
            this.markedHelpfulByIds.add(customerId);
            save();
        }
    }

}


