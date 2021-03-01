package com.arjunsk.codekrypt.di.sample.services;

import com.arjunsk.codekrypt.di.core.annotation.Autowire;
import com.arjunsk.codekrypt.di.core.annotation.Component;
import com.arjunsk.codekrypt.di.core.annotation.PostConstruct;
import com.arjunsk.codekrypt.di.core.annotation.Qualifier;
import com.arjunsk.codekrypt.di.sample.components.horn.Horn;

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
