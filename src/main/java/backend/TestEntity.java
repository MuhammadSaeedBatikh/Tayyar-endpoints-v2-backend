package backend;

import com.google.api.server.spi.types.DateAndTime;
import com.google.api.server.spi.types.SimpleDate;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.repackaged.com.google.protobuf.Timestamp;
import com.google.appengine.repackaged.org.codehaus.jackson.annotate.JsonIgnore;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Muhammad on 26/08/2017.
 */
@Entity
public class TestEntity {
    @Id
    Long id;
    @Index
    String name;
    @Index
    DateAndTime dateAndTime;
    @Index
    Date javaDate;
    @Index
    Long time;
    @Index
    SimpleDate simpleDate;
    @Index
    public List<String> categories = new ArrayList<>();
    @Index
    public List<String> areas  = new ArrayList<>();;
    @Index
    public List<String> anotherFilterList  = new ArrayList<>();;
    @Index
    GeoPt geoPt;
    public String textJson =" ";
    @Index
    public HashMap<String, Long> map = new HashMap<>();

    //default constructor for Entity initialization
    {
        this.javaDate = new Date();
    }
//default constructor for Entity initialization
public TestEntity (){}
//============
    public TestEntity(String category, String area, String anotherFilter) {
        this.categories.add(category);
        this.areas.add(area);
        this.anotherFilterList.add(anotherFilter);
    }


    public void addFilters(String category, String area, String anotherFilter){
        this.categories.add(category);
        this.areas.add(area);
        this.anotherFilterList.add(anotherFilter);
        save();
    }
    //============
    public TestEntity(DateAndTime dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public TestEntity(Date javaDate) {
        this.javaDate = javaDate;

    }

    public void log(Object message){
       this.textJson+=message;
        this.save();
    }

    public TestEntity(SimpleDate simpleDate) {
        this.simpleDate = simpleDate;
    }

    public TestEntity(GeoPt geoPt) {
        this.geoPt = geoPt;
    }

    public static TestEntity getTestEntityById(Long id) {
        return ofy().load().type(TestEntity.class).id(id).now();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateAndTime getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(DateAndTime dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public Date getJavaDate() {
        return javaDate;
    }

    public void setJavaDate(Date javaDate) {
        this.javaDate = javaDate;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public SimpleDate getSimpleDate() {
        return simpleDate;
    }

    public void setSimpleDate(SimpleDate simpleDate) {
        this.simpleDate = simpleDate;
    }

    public GeoPt getGeoPt() {
        return geoPt;
    }

    public void setGeoPt(GeoPt geoPt) {
        this.geoPt = geoPt;
    }

    public void save() {
        ofy().save().entity(this).now();
    }

    @Override
    public String toString() {
        return "TestEntity{" +
                "id=" + id +
                ", textJson='" + textJson + '\'' +
                '}';
    }
}
