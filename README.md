# Codekrypt Injectors

## Feature
- Supports Field Injection using `@Autowire`
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

> Car.java
```java
@Component
public class Car {

  private int odometerReading;

  @Qualifier(value = "HornAirImpl")
  @Autowire
  private Horn horn;

  public Car() {
    odometerReading = 0;
  }

  public void incrementOdometer() {
    odometerReading++;
  }

  @PostConstruct
  public void honk() {
    horn.honk();
  }
}
```

> CarApplication.java
```java
public class CarApplication {

  public static void main(String[] args) {
    CodekryptInjector.run(CarApplication.class);
  }
}
```

### Output
````text
Air Horn Initialized. 
Air Horn Implementation called.
````


### TODO
- Implement Constructor Injection.
- Fix Diamond Problem DI.
- Fix class load in JAR (after mvn package).