package com.arjunsk.codekrypt.di.sample.services;

import com.arjunsk.codekrypt.di.annotation.Autowire;
import com.arjunsk.codekrypt.di.annotation.Component;
import com.arjunsk.codekrypt.di.annotation.PostConstruct;
import com.arjunsk.codekrypt.di.annotation.Qualifier;
import com.arjunsk.codekrypt.di.sample.components.horn.Horn;

/** Field Injection. */
@Component
public class TruckService {

  private int odometerReading;

  @Qualifier(value = "HornElectricImpl")
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
    horn.honk();
  }
}
