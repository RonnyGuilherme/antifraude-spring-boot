package br.com.seuprojeto.antifraude.strategy;

public record RuleResult(
        boolean fraudDetected,
        String reason
) {
    public static RuleResult approved() {
        return new RuleResult(false, null);
    }

    public static RuleResult denied(String reason) {
        return new RuleResult(true, reason);
    }
}
