package io.jans.api;

import java.util.Date;

public class AgamaDeploymentDescriptor {
    
    private String id;
    private boolean active;

    public AgamaDeploymentDescriptor(final String id, final boolean active) {

        this.id = id;
        this.active = active;
    }

    public String getId() {

        return id;
    }

    public boolean isActive() {

        return this.active;
    }
}
