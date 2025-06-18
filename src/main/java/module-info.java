module com.example.jpo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires redis.clients.jedis;
    requires com.fasterxml.jackson.databind;


    opens com.example.jpo to javafx.fxml;
    exports com.example.jpo;
}