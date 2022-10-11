package com.example.sample_lib_ksp;

import javax.inject.Named;

import motif.Scope;

@Scope
public interface JavaScope {
  String greeter();

  @motif.Objects
  class Objects {

    @Named("name")
    String name() {
      return "World";
    }

    Greeter greeter(@Named("name") String name) {
      return new Greeter(name);
    }
  }

  interface Dependencies {
  }
}
