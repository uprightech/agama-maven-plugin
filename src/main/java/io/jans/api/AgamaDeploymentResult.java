package io.jans.api;

import java.text.MessageFormat;

public class AgamaDeploymentResult {
    
    private AgamaDeploymentStatus status;
    private final String message;

    private AgamaDeploymentResult(final AgamaDeploymentStatus status, final String message) {

        this.status = status;
        this.message = message;
    }
    
    public boolean deploymentInitiated() {

        return status != AgamaDeploymentStatus.DEPLOYMENT_ERROR;
    }

    public final String getMessage() {

        return this.message;
    }

    public static AgamaDeploymentResult deploymentError(final int httpcode, final String message) {

        final String finalmsg = MessageFormat.format("Deployment error (HTTP code {0}). {1}",httpcode,message);
        return new AgamaDeploymentResult(AgamaDeploymentStatus.DEPLOYMENT_ERROR, finalmsg);
    }


    public static AgamaDeploymentResult deploymentPending(final String message) {

        return new AgamaDeploymentResult(AgamaDeploymentStatus.DEPLOYMENT_PENDING, message);
    }

    public static AgamaDeploymentResult deploymentOk(final String message) {

        return new AgamaDeploymentResult(AgamaDeploymentStatus.DEPLOYMENT_SUCCESS,message);
    }
}
