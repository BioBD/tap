/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap;

import br.pucrio.biobd.tap.agents.Predictor;
import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.ConnectionSGBD;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.libraries.ReadXML;
import br.pucrio.biobd.tap.agents.sgbd.ReadSchemaDB;
import jade.Boot;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 * @author Rafael Pereira
 */
public class StartAgents {

    public static Profile profile;
    public static AgentContainer controllerAgentContainer;

    public static void main(String args[]) {
        StartAgents.executeInitialization();
        Boot.main(new String[]{"-gui"});
        ArrayList<StartAgents> agents = new ArrayList();
        ReadSchemaDB.getSchemaDB();
        try {
            List<String> agentsClasses = getAgentNames();
            for (String agentClass : agentsClasses) {
                Log.msg("Initialize agent : " + agentClass);
                Class<?> c = Class.forName(agentClass);
                agents.add(new StartAgents((Agent) c.newInstance(), c.getSimpleName(), "TAP"));
                Log.msg("Agent initialized: " + agentClass);
            }

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            System.err.print(ex);
        }
    }

    public static List<String> getAgentNames() {
        ReadXML read = new ReadXML();
        return read.getNodeValues("config/algoritms.xml", "initialize", "type", "Agent");
    }

    public static List<String> getPredictorAgentNames() {
        List<String> allAgents = getAgentNames();
        List<String> predictorAgents = new ArrayList<>();
        for (String allAgent : allAgents) {
            try {
                String agent = controllerAgentContainer.getAgent(Class.forName(allAgent).getSimpleName()).getName();
                if (agent != null && Predictor.class.isAssignableFrom(Class.forName(allAgent))) {
                    predictorAgents.add(Class.forName(allAgent).getSimpleName());
                }
            } catch (ControllerException | ClassNotFoundException ex) {
                Logger.getLogger(StartAgents.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return predictorAgents;
    }

    public static String firstOmegaAgentName() {
        List<String> predictorAgents = getPredictorAgentNames();
        if (predictorAgents.size() > 0) {
            return predictorAgents.get(0);
        } else {
            return null;
        }
    }

    public static String getNextOmegaAgentName(String actual) {
        List<String> predictorAgents = getPredictorAgentNames();
        for (int i = 0; i < predictorAgents.size(); i++) {
            String agent = predictorAgents.get(i);
            if (agent.equals(actual)) {
                if ((i + 1) < predictorAgents.size()) {
                    return predictorAgents.get(i + 1);
                } else {
                    return predictorAgents.get(0);
                }
            }
        }

        if (predictorAgents.size() > 0) {
            return predictorAgents.get(0);
        } else {
            return null;
        }
    }

    private static void executeInitialization() {
        String[] scriptsInitialization = Config.getProperty("scriptInitialization" + Config.getProperty("sgbd")).split(";");
        ConnectionSGBD conn = new ConnectionSGBD();
        for (String script : scriptsInitialization) {
            try {
                if (!script.isEmpty()) {
                    PreparedStatement preparedStatement = conn.connection("main").prepareStatement(script);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                }
                Log.clearFolder(Config.getProperty("folderTuningAction"));
            } catch (SQLException e) {
                Log.msg(script);
                Log.error(e);
            }
        }
    }

    public StartAgents(Agent agent, String nameAgent, String nameContainer) {
        setAgentInContainer(agent, nameAgent, nameContainer);
    }

    private void setAgentInContainer(Agent agent, String nameAgent, String nameContainer) {
        Runtime runtime = Runtime.instance();
        if (profile == null) {
            StartAgents.profile = new ProfileImpl();
            profile.setParameter(Profile.CONTAINER_NAME, nameContainer);
            StartAgents.controllerAgentContainer = runtime.createAgentContainer(profile);
        }
        try {
            AgentController controller = controllerAgentContainer.acceptNewAgent(nameAgent, agent);
            controller.start();
        } catch (StaleProxyException ex) {
            Log.error(ex);
        }
    }
}
