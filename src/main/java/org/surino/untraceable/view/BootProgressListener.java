package org.surino.untraceable.view;

import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;

public class BootProgressListener implements ApplicationListener<SpringApplicationEvent> {

    private final SplashScreen splash;

    public BootProgressListener(SplashScreen splash) {
        this.splash = splash;
    }

    @Override
    public void onApplicationEvent(SpringApplicationEvent e) {

        if (e instanceof ApplicationStartingEvent) {
            splash.setProgress(5, "Avvio…");

        } else if (e instanceof ApplicationEnvironmentPreparedEvent) {
            splash.setProgress(20, "Preparazione ambiente…");

        } else if (e instanceof ApplicationContextInitializedEvent) {
            splash.setProgress(40, "Inizializzazione contesto…");

        } else if (e instanceof ApplicationPreparedEvent) {
            splash.setProgress(60, "Creazione componenti…");

        } else if (e instanceof ApplicationStartedEvent) {
            splash.setProgress(80, "Avvio moduli…");

        } else if (e instanceof ApplicationReadyEvent) {
            splash.setProgress(100, "Pronto!");
        }
    }
}
