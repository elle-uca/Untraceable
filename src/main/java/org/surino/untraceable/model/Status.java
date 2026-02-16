package org.surino.untraceable.model;

public enum Status {

    SCONOSCIUTO("Sconosciuto"),
    IRREPERIBILE("Irreperibile"),
    CANCELLATO("Cancellato"),
    DECEDUTO("Deceduto"),
    EMIGRATO("Emigrato"),
    ISCRITTO_AIRE("Iscritto Aire"),
    MAI_RESIDENTE("Mai Residente");

    private final String name;

    Status(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}

