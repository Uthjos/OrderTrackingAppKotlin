package org.metrostate.ics.ordertrackingappkotlin

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class HelloApplication : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("main-view.fxml"))
        val root = fxmlLoader.load<javafx.scene.Parent>()
        val scene = Scene(root)
        stage.title = "Order Tracker"
        stage.scene = scene
        stage.show()
    }
}
