package org.htwkvisu.controller;

import org.junit.Before;

public class ApplicationControllerTest {

    private ApplicationController ctrl;

    @Before
    public void setUp() throws Exception {
        ctrl = new ApplicationController();
    }

    // JavaFX GUI testing only works with external FX testing framework
    // because: all variables for GUI fields are here not injected and lead to a null pointer
}