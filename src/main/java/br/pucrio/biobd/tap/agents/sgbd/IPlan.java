/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd;

/**
 *
 * @author Rafael
 */
public interface IPlan {

    public long getCost();

    public long getNumRow();

    public long getSizeRow();

    public float getDuration();

}
