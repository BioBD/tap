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
public class AcceptedRequest extends Message {

    public AcceptedRequest(String sourceID) {
        super(sourceID);
    }

    @Override
    public String toString() {
        return "AcceptedMessage [proposalNumber=" + getNumber()
                + ", sourceId=" + getSourceID() + "]";
    }

    @Override
    public MessageType getType() {
        return MessageType.ACCEPTED;
    }
}
