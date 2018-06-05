package com.revolut.transfer.exceptions;

/**
 * <p></p>Any child of this exception will be transformed in a 406 Not Acceptable by the REST layer</p>
 * <p>On this project, there is only one child, but this class should be used as the base class when there is an
 * situation where a given functionality cannot be executed because of a broken requirement</p>
 */
public abstract class NotAcceptableException extends RevolutException {
}
