package com.arjunsk.codekrypt.di.sample.components.horn.impl;

import com.arjunsk.codekrypt.di.annotation.Component;
import com.arjunsk.codekrypt.di.annotation.PostConstruct;
import com.arjunsk.codekrypt.di.sample.components.horn.Horn;

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
