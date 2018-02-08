module kikstrava {
    requires javafx.controls;
    requires jsoup;

    requires transitive jackson.core;
    requires transitive jackson.databind;
	requires transitive jackson.annotations;
    requires transitive javafx.base;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;

    exports kikstrava;
    exports kikstrava.controller;
    exports kikstrava.model;
    exports kikstrava.service;
	
	opens kikstrava.controller to javafx.fxml;

}
