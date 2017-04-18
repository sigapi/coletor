package br.edu.fatecsbc.sigapi.coletor.service;

public class CredenciaisInvalidasException
    extends SigapiException {

    private static final long serialVersionUID = 1L;

    public CredenciaisInvalidasException() {
        super("Credenciais inválidas");
    }

}
