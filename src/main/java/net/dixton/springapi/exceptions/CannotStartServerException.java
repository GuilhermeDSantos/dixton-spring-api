package net.dixton.springapi.exceptions;

import net.dixton.enums.ExceptionError;
import net.dixton.enums.HttpStatus;
import net.dixton.exceptions.DixtonRuntimeException;
import net.dixton.model.server.Server;

public class CannotStartServerException extends DixtonRuntimeException {
    public CannotStartServerException(Server server) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionError.CANNOT_START_SERVER, "Cannot start the server: " + server);
    }

    public CannotStartServerException(Server server, Exception exception) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionError.CANNOT_START_SERVER, "Ocurred an error while starting the server " + server.getName() + ". Error: " + exception.getMessage());
    }
}
