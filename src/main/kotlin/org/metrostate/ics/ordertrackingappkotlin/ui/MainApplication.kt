package org.metrostate.ics.ordertrackingappkotlin

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class MainApplication : Application() {
    private var mainViewController: MainViewController? = null

    companion object {
        var mainViewRoot: javafx.scene.Parent? = null
            private set
    }

    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(MainApplication::class.java.getResource("main-view.fxml"))
        val root = fxmlLoader.load<javafx.scene.Parent>()

        mainViewRoot = root

        mainViewController = fxmlLoader.getController()

        val scene = Scene(root)
        stage.title = "Order Tracker"
        stage.scene = scene

        stage.setOnCloseRequest {
            mainViewController?.shutdown()
        }

        stage.show()
    }
}
