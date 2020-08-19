/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

import java.util.ArrayList;

/**
 *
 * @author Rafael
 */
public class Schema {

    public ArrayList<Table> tables;

    public Schema() {
        this.tables = new ArrayList<>();

    }

}
