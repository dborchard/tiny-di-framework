package com.arjunsk.codekrypt.di.sample.components.horn.impl;

import com.arjunsk.codekrypt.di.annotation.Component;
import com.arjunsk.codekrypt.di.sample.components.horn.Horn;

@Component
public class HornElectricImpl implements Horn {

  @Override
  public void honk() {
    System.out.println("Electric Horn Implementation called");
  }
}
