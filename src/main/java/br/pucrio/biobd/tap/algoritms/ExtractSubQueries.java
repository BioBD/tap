/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Rafael
 */
public class ExtractSubQueries {

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

    private int countWord(String text, String word) {
        Pattern p = Pattern.compile(word);
        Matcher m = p.matcher(text);
        int count = 0;
        while (m.find()) {
            count += 1;
        }
        return count;
    }

    private static class Start {

        final int brace;
        final int pos;

        public Start(int brace, int pos) {
            this.brace = brace;
            this.pos = pos;
        }

    }

    public ArrayList<String> extractSubQueries(String sqlText) {
        ArrayList<String> result = new ArrayList<>();
        for (String subString : this.split(sqlText, "(", ")")) {
            if (!filter(subString).isEmpty()) {
                String sqlExtracted = getSubComment(sqlText) + subString.substring(1, subString.length() - 1);
                if (!result.contains(sqlExtracted)) {
                    result.add(sqlExtracted);
                }
            }
        }
        return result;
    }

    private String getSubComment(String sqlText) {
        int ini = sqlText.indexOf("/*");
        if (ini >= 0) {
            int end = sqlText.substring(ini).indexOf("*/") + ini + 2;
            String result = sqlText.substring(ini, end).trim() + " ";
            sqlText = sqlText.replace(result, "");
            if (sqlText.contains("/*")) {
                return result + " " + getSubComment(sqlText);
            } else {
                return result;
            }
        } else {
            return "";
        }
    }

    private String filter(String query) {
        if (query.toLowerCase().contains("select") && query.toLowerCase().contains("from")) {
            if (this.countWord(query.toLowerCase(), "select") == 1) {
                return query;
            }
        }
        return "";
    }

    public String fixSubQueryFromWrongField(String sql, String column) {
        String[] clause = sql.split(" ");
        ArrayList<String> result = new ArrayList<>();

        int j = 0;
        for (int i = 0; i < clause.length; i++) {
            if (!clause[i].toLowerCase().equals(column.toLowerCase())) {
                result.add(clause[i]);
            } else if ((i < clause.length - 1) && isOperator(clause[i + 1])) {
                i = i + 2;
                if ((i < clause.length - 1) && isLogicKey(clause[i + 1])) {
                    i = i + 1;
                }
            } else {
                result.remove(result.size() - 1);
                result.remove(result.size() - 1);
                if ((i < clause.length - 1) && isLogicKey(result.get(result.size() - 1)) && isLogicKey(clause[i + 1])) {
                    result.remove(result.size() - 1);
                }
                if ((i < clause.length - 1) && isLogicClauseKey(result.get(result.size() - 1)) && isLogicKey(clause[i + 1])) {
                    i = i + 1;
                }
            }
        }
        String queryFixed = "";
        for (int i = result.size() - 1; i >= 0; i--) {
            if (!(queryFixed.isEmpty() && isLogicClauseKey(result.get(i)))) {
                queryFixed = result.get(i) + " " + queryFixed;
            }
        }
        return queryFixed;
    }

    private boolean isOperator(String text) {
        return text.toLowerCase().equals("<")
                || text.toLowerCase().equals(">")
                || text.toLowerCase().equals("<=")
                || text.toLowerCase().equals("<>")
                || text.toLowerCase().equals(">=")
                || text.toLowerCase().equals("=");
    }

    private boolean isLogicKey(String text) {
        return text.toLowerCase().equals("and")
                || text.toLowerCase().equals("or");
    }

    private boolean isLogicClauseKey(String text) {
        return text.toLowerCase().equals("where");
    }

}
