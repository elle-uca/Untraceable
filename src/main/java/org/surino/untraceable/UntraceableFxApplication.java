package org.surino.untraceable;


import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.surino.untraceable.view.BootProgressListener;
import org.surino.untraceable.view.PersonView;
import org.surino.untraceable.view.SplashScreen;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UntraceableFxApplication extends Application {

    private ConfigurableApplicationContext springContext;
    private SplashScreen splash;

    @Override
    public void start(Stage stage) {

        splash = new SplashScreen();
        splash.showSplash();

        stage.getIcons().add(
                new javafx.scene.image.Image(
                        getClass().getResourceAsStream("/img/untreceable-24.png")
                )
        );
        
        
        // Spring in background thread
        new Thread(() -> {

            SpringApplicationBuilder builder =
                    new SpringApplicationBuilder(SpringContextConfig.class)
                            .web(WebApplicationType.NONE)
                            .headless(false);

            builder.listeners(new BootProgressListener(splash));

            springContext = builder.run();

            // Quando Spring ha finito → mostra UI
            Platform.runLater(() -> {

                Application.setUserAgentStylesheet(
                        new PrimerLight().getUserAgentStylesheet());

                PersonView root = springContext.getBean(PersonView.class);

                Scene scene = new Scene(root, 1000, 700);
                scene.getStylesheets().add(
                        getClass().getResource("/css/untraceable.css").toExternalForm()
                );

                stage.setTitle("UntraceableFX by Luke");
                stage.setScene(scene);
                stage.show();

                splash.close();
            });

        }).start();
    }

    @Override
    public void stop() {
        if (springContext != null) springContext.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
