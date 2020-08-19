/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms.MaterializedView;

import br.pucrio.biobd.tap.agents.sgbd.models.implementors.PlanImplementor;
import java.util.ArrayList;

/**
 *
 * @author Rafael
 */
public class Combinacao {

    private static final String openBraces = "({(<";
    private static final String closeBraces = ")})>";
    private final ArrayList<String> result;
    private final ArrayList<String> termos;

    public Combinacao() {
        this.result = new ArrayList<>();
        this.termos = new ArrayList<>();
    }

    private ArrayList<String> split(String s) {
        if (s.contains("(")) {
            return split(s, openBraces, closeBraces);
        } else {
            return new ArrayList<>();
        }
    }

    public ArrayList<String> dividirExpressaoPredicado(String where) {
        where = this.removeWhere(where.trim());
        this.termos.addAll(this.split(where));
        this.dividir(where);
        this.splitAll(where);
        return this.getResultadoFinal();
    }

    private String removeWhere(String where) {
        if (where.trim().substring(0, 5).contains("where")) {
            where = where.substring(6);
        }
        return where.trim();
    }

    private void splitAll(String where) {
        PlanImplementor planImp = PlanImplementor.createPlanImplementor();
        String[] restrictions = planImp.splitRestrictions(where);
        for (String restriction : restrictions) {
            this.result.add(restriction);
        }
    }

    private static class Start {

        final int brace;
        final int pos;

        public Start(int brace, int pos) {
            this.brace = brace;
            this.pos = pos;
        }

        @Override
        public String toString() {
            return "{" + openBraces.charAt(brace) + "," + pos + "}";
        }
    }

    private ArrayList<String> split(String s, String open, String close) {
        ArrayList<Start> stack = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            int o = open.indexOf(ch);
            int c = close.indexOf(ch);
            if (o >= 0) {
                stack.add(new Start(o, i));
            } else if (c >= 0 && stack.size() > 0) {
                int tosPos = stack.size() - 1;
                Start tos = stack.get(tosPos);
                if (tos.brace == c) {
                    if (!result.contains(s.substring(tos.pos, i + 1))) {
                        result.add(s.substring(tos.pos, i + 1));
                    }
                    stack.remove(tosPos);
                }
            }
        }
        return result;
    }

    private ArrayList<String> getResultadoFinal() {
        ArrayList<String> saida = new ArrayList<>();
        for (String termo : this.result) {
            if (!saida.contains(termo.trim())) {
                saida.add(termo.trim());
            }
        }
        return saida;
    }

    private boolean dividir(String termo) {
        termo = termo.toLowerCase();
        if (termo.isEmpty()) {
            return true;
        }
        this.result.add(termo);

        if (!(termo.contains(" and ") || termo.contains(" or "))) {
            return true;
        }
        if (this.termos.contains(termo)) {
            termo = termo.substring(1, termo.length() - 1);
        }

        String[] palavras = termo.split(" ");
        int abriu = 0;
        int fechou = 0;

        for (int i = palavras.length - 1; i >= 0; i--) {
            if (palavras[i].contains("(")) {
                abriu += palavras[i].replaceAll("[^\\(]", "").length();
            }
            if (palavras[i].contains(")")) {
                fechou += palavras[i].replaceAll("[^\\)]", "").length();
            }

            if ((palavras[i].equals("or") || palavras[i].equals("and")) && (abriu == fechou)) {
                String anterior = "";

                for (int j = 0; j < i; j++) {
                    anterior += " " + palavras[j];
                }
                dividir(anterior.trim());

                String posterior = "";
                for (int j = i + 1; j < palavras.length; j++) {
                    posterior += " " + palavras[j];
                }
                if (posterior.contains("(")) {
                    dividir(posterior.trim());
                } else {
                    this.result.add(posterior);
                }
                return true;
            }
        }
        return true;

    }
}
