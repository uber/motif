package com.example.sample_lib_ksp;

class Greeter {
  private final String name;

  public Greeter(String name) {
    this.name = name;
  }

  String greet() {
    return "Hello " + name + "!";
  }
}
