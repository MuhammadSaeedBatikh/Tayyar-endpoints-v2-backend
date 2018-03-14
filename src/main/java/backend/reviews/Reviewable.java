package backend.reviews;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;

import java.util.ArrayList;
import java.util.List;

import backend.stats.ReviewStats;

/**
 * Created by Muhammad on 03/02/2018.
 */

public interface Reviewable {
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    List<Key<Review>> reviews = new ArrayList<Key<Review>>();
    ReviewStats reviewStats = new ReviewStats();
}
