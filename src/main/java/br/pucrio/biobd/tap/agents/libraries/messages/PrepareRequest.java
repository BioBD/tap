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
public class PrepareRequest extends Message {

    public PrepareRequest(String sourceID) {
        super(sourceID);
    }

    @Override
    public MessageType getType() {
        return MessageType.PREPARE;
    }

    @Override
    public String toString() {
        return "PrepareMessage [proposalNumber=" + getNumber()
                + ", sourceId=" + getSourceID() + "]";
    }
}
