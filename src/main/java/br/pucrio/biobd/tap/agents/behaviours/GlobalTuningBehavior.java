/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.behaviours;

import br.pucrio.biobd.tap.StartAgents;
import br.pucrio.biobd.tap.agents.BasicAgent;
import br.pucrio.biobd.tap.agents.Predictor;
import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.libraries.messages.GlobalTuningMessage;
import br.pucrio.biobd.tap.agents.libraries.messages.Message;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.agents.sgbd.models.dao.TuningActionDAO;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael
 */
public abstract class GlobalTuningBehavior extends TickerBehaviour {

    List<TuningAction> tuningActionsListIn;
    List<TuningAction> tuningActionsListOut;
    private static long countProposalNumber = 1;

    public GlobalTuningBehavior(Agent a, long period) {
        super(a, period);
        this.tuningActionsListIn = new ArrayList<>();
        this.tuningActionsListOut = new ArrayList<>();
    }

    @Override
    protected void onTick() {
        if (Config.getProperty("globalTuningActive").equals("1")) {
            this.receiveMessages();
            this.ratingTuningActions();
            this.insertTuningActions();
        }
    }

    protected void startRoundGlobalTuning() {
        this.tuningActionsListIn = ((Predictor) this.myAgent).tuningActions;
        if (!this.tuningActionsListIn.isEmpty()) {
            this.sendMyAgentTuningActions(null);
        }
        this.transferOmega();
    }

    private void receiveMessages() {
        ACLMessage msg = this.myAgent.receive();
        if (msg != null) {
            printMsg(msg);
            if (msg instanceof GlobalTuningMessage) {
                this.generateGlobalTuningActions((GlobalTuningMessage) msg);
                this.sendGlobalTuningActionsToOtherAgents((GlobalTuningMessage) msg);
            }
        }
    }

    protected boolean myAgentIsOmega() {
        if (Predictor.omega == null) {
            Predictor.omega = StartAgents.firstOmegaAgentName();
        }
        return Predictor.omega != null && this.myAgent.getClass().getSimpleName().equals(Predictor.omega);
    }

    private void sendMyAgentTuningActions(GlobalTuningMessage msgIn) {
        try {
            String myAgentName = this.myAgent.getClass().getSimpleName();
            GlobalTuningMessage msg = new GlobalTuningMessage(myAgentName);
            if (msgIn != null) {
                msg.agents.addAll(msgIn.agents);
                msg.setNumber(msgIn.getNumber());
            } else {
                msg.setNumber(countProposalNumber++);
            }
            msg.agents.add(myAgentName);
            List<String> agentNames = StartAgents.getPredictorAgentNames();
            msg.actions = new ArrayList<>(this.tuningActionsListIn);
            for (String agentName : agentNames) {
                if (!msg.agents.contains(agentName)) {
                    AID r = new AID(agentName, AID.ISLOCALNAME);
                    msg.addReceiver(r);
                    msg.setContent("GlobalTuningTask");
                }
            }
            this.myAgent.send(msg);
        } catch (Error ex) {
            Log.error(ex);
        }
    }

    private void transferOmega() {
        Predictor.omega = StartAgents.getNextOmegaAgentName(Predictor.omega);
    }

    private void generateGlobalTuningActions(GlobalTuningMessage msg) {
        this.tuningActionsListOut = new ArrayList<>();
        if (msg.actions != null) {
            for (TuningAction action : msg.actions) {
                List<TuningAction> globalAction = this.generateGlobalAction(action);
                if (globalAction != null) {
                    this.tuningActionsListOut.addAll(globalAction);
                }
            }
        }
    }

    private void sendGlobalTuningActionsToOtherAgents(GlobalTuningMessage msg) {
        this.sendMyAgentTuningActions(msg);
    }

    private void printMsg(ACLMessage msg) {
        String myAgentName = this.myAgent.getClass().getSimpleName();
        String otherAgentName = msg.getSender().getLocalName();
        Log.msg(otherAgentName + " ----- " + msg.getClass().getSimpleName() + " #" + ((Message) msg).getNumber() + " ---->" + myAgentName);

    }

    protected abstract List<TuningAction> generateGlobalAction(TuningAction action);

    protected void saveTuningActionAgent(TuningAction action) {
        action.agents.add(agentName());
        TuningActionDAO tnaDao = action.getInstanceDAO(agentName());
        tnaDao.insertTuningActionAgent(action);
    }

    protected String agentName() {
        return ((BasicAgent) getAgent()).getClass().getSimpleName();
    }

    protected void insertTuningActions() {
        if (this.tuningActionsListOut != null && !this.tuningActionsListOut.isEmpty()) {
            for (int i = 0; i < this.tuningActionsListOut.size(); ++i) {
                TuningAction globalAction = this.tuningActionsListOut.get(i);
                TuningActionDAO tnaDao = TuningActionDAO.getInstance(globalAction, agent().getClass().getSimpleName());
                tnaDao.save(globalAction);
            }
            this.printToScreenTest();
            this.updateBenefitHeuristic();
        }

    }

    private Predictor agent() {
        return ((Predictor) getAgent());
    }

    private void updateBenefitHeuristic() {
        try {
            PreparedStatement preparedStatement = agent().getConnection().prepareStatement(Config.getProperty("updateTbTuningActionBenefitHeuristic_" + Config.getProperty("sgbd")));
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            Log.error(e);
        }
    }

    protected void ratingTuningActions() {
        for (int i = 0; i < this.tuningActionsListOut.size(); ++i) {
            this.tuningActionsListOut.get(i).rating(agent().getClass().getSimpleName());
        }
    }

    protected void printToScreenTest() {
    }

    public abstract ArrayList<TuningAction> getAllTuningActions();

}
