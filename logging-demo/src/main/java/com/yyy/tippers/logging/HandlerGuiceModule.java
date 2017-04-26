package com.yyy.tippers.logging;

/*
  Essentially,
 */

import com.google.inject.multibindings.MapBinder;
import com.yyy.tippers.logging.factory.*;
import com.yyy.tippers.logging.handlers.*;

/*
  Two things being done in configure():
    1. Build mapping String to specific handler class
        Essentially, the mapping being built here decides what kind of handler should be injected via LoggingHandlerFactory.
        Singleton factory class with the map then decides what kind of handler should be further injected via LoggingService.

    2. bind concrete factory class to abstract factory class
 */

public class HandlerGuiceModule extends com.google.inject.AbstractModule {

    @Override
    protected void configure() {
        MapBinder<String, Handlerable> mapBinder = MapBinder.newMapBinder(binder(), String.class, Handlerable.class);

        mapBinder.addBinding("XML").to(HandlerForXML.class);
        mapBinder.addBinding("JSON").to(HandlerForJSON.class);

        bind(HandlerFactory.class).to(LoggingHandlerFactory.class);
    }
}
