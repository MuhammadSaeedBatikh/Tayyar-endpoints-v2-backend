package backend.profiles.driver;

/**
 * Created by Muhammad on 20/01/2018.
 */

public class DriverInfo {
    public Long driverId;
    public String name;
    public String phone;
    public Long cityId;

    public DriverInfo(Driver driver) {
        this.driverId = driver.id;
        this.name = driver.name;
        this.phone = driver.phone;
        this.cityId = driver.cityId;
    }

    public static DriverInfo getDriverInfo(Long driverId){
        return new DriverInfo(Driver.getDriverByID(driverId));
    }

    @Override
    public String toString() {
        return "DriverInfo{" +
                "driverId=" + driverId +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", cityId=" + cityId +
                '}';
    }
}
