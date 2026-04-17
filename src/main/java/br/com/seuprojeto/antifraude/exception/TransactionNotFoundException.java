package br.com.seuprojeto.antifraude.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(Long id) {
        super("Transação não encontrada. ID: " + id);
    }
}