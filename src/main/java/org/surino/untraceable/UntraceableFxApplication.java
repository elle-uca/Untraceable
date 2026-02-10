package org.surino.untraceable;


import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.surino.untraceable.view.PersonView;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UntraceableFxApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext =
            new SpringApplicationBuilder(SpringContextConfig.class)
                .web(WebApplicationType.NONE)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage stage) {
    	PersonView root = springContext.getBean(PersonView.class);

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(
        	    getClass().getResource("/css/untraceable.css").toExternalForm()
        	);
        stage.setTitle("UntraceableFX");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

