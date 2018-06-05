package com.revolut.transfer.exceptions;

/**
 * <p></p>Any child of this exception will be transformed in a 404 Not Found by the REST layer.</p>
 * <p>On this project, there is only one child, but this class should be used as the base class when there is an
 * unsuccessful load of any entity</p>
 *
 */
public abstract class ResourceNotFoundException extends RevolutException {
}
