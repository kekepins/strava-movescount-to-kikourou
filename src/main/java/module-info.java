open module kikstrava {


    requires org.jsoup;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires javafx.controls;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.fxml;
    requires jdk.httpserver;
    requires jdk.crypto.ec;


	//requires transitive jdk.httpserver;
    //requires transitive com.fasterxml.jackson.core;
    //requires transitive com.fasterxml.jackson.databind;
	//requires transitive com.fasterxml.jackson.annotation;
    //requires transitive javafx.base;
    //requires transitive javafx.fxml;
    //requires transitive javafx.graphics;
    //requires org.jsoup;

    exports kikstrava;
    exports kikstrava.controller;
    exports kikstrava.model;
    exports kikstrava.service;
	
	//opens kikstrava.controller to javafx.fxml;

}
