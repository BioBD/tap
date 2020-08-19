/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.libraries.messages;

/**
 *
 * @author Rafael
 */
public class TokenOmega extends Message {

    public TokenOmega(String sourceID) {
        super(sourceID);
    }

    @Override
    public MessageType getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
