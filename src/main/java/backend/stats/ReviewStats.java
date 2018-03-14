package backend.stats;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 03/02/2018.
 */

@Entity
@Cache
public class ReviewStats {
    @Id
    public Long id;
    @Index
    public Long revieweeId; //merchantId
    public HashMap<String, Integer> reviewsMap = new HashMap<>(); //reviewMap<rating, count>
    public int totalReviewsCount;
    public double rating;
    public Date lastUpdate;

    //default constructor for Entity initialization
    public ReviewStats() {

    }


    public void updateWithRating(int rating) {
        String ratingStr = String.valueOf(rating);
        Integer count = this.reviewsMap.get(ratingStr);
        if (count == null) {
            count = 0;
        }
        this.reviewsMap.put(ratingStr, ++count);
        this.totalReviewsCount++;
        updateStats();
        save();
    }

    public void updateStats() {
        HashMap<String, Integer> reviewsMap = this.reviewsMap;
        int totalReviewsCount = this.totalReviewsCount;
        double newRating = 0;
        for (Map.Entry<String, Integer> entry : reviewsMap.entrySet()) {
            Integer rating = Integer.valueOf(entry.getKey());
            Integer count = entry.getValue();
            double addedRating = (double) rating * (double) count / totalReviewsCount;
            newRating += addedRating;
        }
        this.rating = newRating;
        this.lastUpdate = new Date();
    }

    public void save(){
        ofy().save().entities(this).now();
    }

    @Override
    public String toString() {
        return "ReviewStats{" +
                "id=" + id +
                ", revieweeId=" + revieweeId +
                ", reviewsMap=" + reviewsMap +
                ", totalReviewsCount=" + totalReviewsCount +
                ", rating=" + String.valueOf((double) ((int) (rating * 10)) / 10.0) +
                '}';
    }

}
