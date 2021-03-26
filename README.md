# Codekrypt Dependency Injector

## Feature
- Supports Field Injection using `@Autowire` & `@Qualifier`
- Supports `Constructor` Injection.
- Supports `@PostConstruct` 

## Usage
> HornAirImp.java
```java
@Component
public class HornAirImpl implements Horn {

  @Override
  public void honk() {
    System.out.println("Air Horn Implementation called.");
  }

  @PostConstruct
  public void postConstruct() {
    System.out.println("Air Horn Initialized.");
  }
}
```

> CarService.java
```java
/** Constructor Injection. */
@Component
public class CarService {

  private final Horn horn;
  private int odometerReading;

  @Autowire
  public CarService(@Qualifier("HornAirImpl") Horn horn) {
    odometerReading = 0;
    this.horn = horn;
  }

  public void incrementOdometer() {
    odometerReading++;
  }

  @PostConstruct
  public void honk() {
    System.out.print("In Car Service: ");
    horn.honk();
  }
}
```

> TruckService.java
```java
/** Field Injection. */
@Component
public class TruckService {

  private int odometerReading;

  @Qualifier(value = "HornAirImpl")
  @Autowire
  private Horn horn;

  public TruckService() {
    odometerReading = 0;
  }

  public void incrementOdometer() {
    odometerReading++;
  }

  @PostConstruct
  public void honk() {
    System.out.print("In Truck Service: ");
    horn.honk();
  }
}
```
 

> VehicleApplication.java
```java
public class VehicleApplication {

  public static void main(String[] args) {
    CodekryptInjector.run(VehicleApplication.class);
  }
}
```

### Output
````text
In Truck Service: Air Horn Implementation called.
Air Horn Initialized.
In Car Service: Air Horn Implementation called.
````


### TODO
- ~~Implement Constructor Injection.~~
- Post Construct invocation order.
- Fix multiple interface bean resolution.
- Fix class scanning inside fat JAR (ie after mvn clean install).