package com.arjunsk.codekrypt.di.sample.services;

import com.arjunsk.codekrypt.di.annotation.Autowire;
import com.arjunsk.codekrypt.di.annotation.Component;
import com.arjunsk.codekrypt.di.annotation.PostConstruct;
import com.arjunsk.codekrypt.di.annotation.Qualifier;
import com.arjunsk.codekrypt.di.sample.components.horn.Horn;

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
