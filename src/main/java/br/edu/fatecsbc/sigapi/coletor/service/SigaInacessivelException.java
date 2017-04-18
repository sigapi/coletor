package br.edu.fatecsbc.sigapi.coletor.service;

public class SigaInacessivelException
    extends SigapiException {

    private static final long serialVersionUID = 1L;

    public SigaInacessivelException(final String message) {
        super(message);
    }

    public SigaInacessivelException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
