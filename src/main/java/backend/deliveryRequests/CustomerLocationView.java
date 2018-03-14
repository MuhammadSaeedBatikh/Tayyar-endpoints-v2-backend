package backend.deliveryRequests;

/**
 * Created by Muhammad Saeed on 2/11/2017.
 */

public class CustomerLocationView {
    public Long cityId;
    public Long areaId;
    public float longitude;
    public float latitude;
    public String address;
    public String deliveryInstructions;
    public String buildingNumber;
    public String floorNumber;
    public String apartmentNumber;

    public void setValues(Long cityId, Long areaId, float longitude, float latitude, String address, String deliveryInstructions,
                                String buildingNumber, String floorNumber, String apartmentNumber) {
        this.cityId = cityId;
        this.areaId = areaId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
        this.deliveryInstructions = deliveryInstructions;
        this.buildingNumber = buildingNumber;
        this.floorNumber = floorNumber;
        this.apartmentNumber = apartmentNumber;
    }
}
