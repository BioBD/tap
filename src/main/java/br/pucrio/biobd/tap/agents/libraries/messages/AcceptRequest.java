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
public class AcceptRequest extends Message {

    public AcceptRequest(String sourceID) {
        super(sourceID);
    }

    @Override
    public MessageType getType() {
        return MessageType.ACCEPT;
    }

    @Override
    public String toString() {
        return "AcceptRequest [proposalNumber=" + getNumber() + "]";
    }

}
