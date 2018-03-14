package backend.reviews;

import java.util.ArrayList;
import java.util.List;

import backend.stats.ReviewStats;

/**
 * Created by Muhammad on 03/02/2018.
 */

public class MerchantReviewsViews {
    public ReviewStats reviewStats;
    public List<Review> reviews = new ArrayList<>();
    public int reviewsCount;

    public MerchantReviewsViews(ReviewStats reviewStats, List<Review> reviews) {
        this.reviewStats = reviewStats;
        this.reviews = reviews;
        this.reviewsCount = reviews.size();
    }
}
