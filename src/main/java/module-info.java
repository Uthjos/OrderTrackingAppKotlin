module org.metrostate.ics.ordertrackingappkotlin {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires org.json;
    requires java.xml;


    opens org.metrostate.ics.ordertrackingappkotlin to javafx.fxml;
    exports org.metrostate.ics.ordertrackingappkotlin;
}