package backend.helpers;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 05/08/2017.
 */

public class CursorHelper<T> {
    public Class<T> classType;
    public Cursor cursor;
    public List<T> result = new ArrayList<>();

    public CursorHelper(Class<T> classType) {
        this.classType = classType;
    }

    public CollectionResponse<T> queryAtCursor(Query<T> query, String cursorStr) {
        if (cursorStr != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursorStr));
        }
        QueryResultIterator<T> iterator = query.iterator();
        while (iterator.hasNext()) {
            T genericObject = iterator.next();
            this.result.add(genericObject);
        }
        this.cursor = iterator.getCursor();
        return buildCollectionResponse();
    }

    public CollectionResponse<T> buildCollectionResponse() {
        CollectionResponse<T> response = CollectionResponse.<T>builder()
                .setItems(this.result)
                .setNextPageToken(this.cursor.toWebSafeString())
                .build();
        return response;
    }

    public CollectionResponse buildCollectionResponse(List list) {
        CollectionResponse response = CollectionResponse.builder()
                .setItems(list)
                .setNextPageToken(this.cursor.toWebSafeString())
                .build();
        return response;
    }
    public static CollectionResponse buildCollectionResponse(List list, Cursor cursor){
        CollectionResponse response = CollectionResponse.builder()
                .setItems(list)
                .setNextPageToken(cursor.toWebSafeString())
                .build();
        return response;

    }
    public Cursor getCursor() {
        return cursor;
    }


}
