package br.edu.fatecsbc.sigapi.coletor.service;

public class SigapiException
    extends Exception {

    private static final long serialVersionUID = 1L;

    public SigapiException() {
        super();
    }

    public SigapiException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SigapiException(final String message) {
        super(message);
    }

    public SigapiException(final Throwable cause) {
        super(cause);
    }

}
