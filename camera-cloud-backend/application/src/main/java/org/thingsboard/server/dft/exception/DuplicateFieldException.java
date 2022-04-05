package org.thingsboard.server.dft.exception;

import lombok.Data;

@Data
public class DuplicateFieldException extends Exception{
  public final String message;

  public DuplicateFieldException(String message) {
    this.message = message;
  }
}
