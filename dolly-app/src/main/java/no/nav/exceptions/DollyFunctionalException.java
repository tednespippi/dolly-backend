package no.nav.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class DollyFunctionalException extends RuntimeException {
	public DollyFunctionalException() {
	}
	
	public DollyFunctionalException(String message) {
		super(message);
	}
	
	public DollyFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public DollyFunctionalException(Throwable cause) {
		super(cause);
	}
	
	public DollyFunctionalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
