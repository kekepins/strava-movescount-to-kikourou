open module kikstrava {
    requires javafx.controls;
    requires jsoup;

	requires transitive jdk.httpserver;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
	requires transitive com.fasterxml.jackson.annotation;
    requires transitive javafx.base;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;

    exports kikstrava;
    exports kikstrava.controller;
    exports kikstrava.model;
    exports kikstrava.service;
	
	//opens kikstrava.controller to javafx.fxml;

}
