module org.metrostate.ics.ordertrackingappkotlin {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;


    opens org.metrostate.ics.ordertrackingappkotlin to javafx.fxml;
    exports org.metrostate.ics.ordertrackingappkotlin;
}