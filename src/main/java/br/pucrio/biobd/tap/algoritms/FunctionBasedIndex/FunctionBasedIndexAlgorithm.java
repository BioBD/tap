/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms.FunctionBasedIndex;

import br.pucrio.biobd.tap.agents.sgbd.models.EnumPlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.FunctionBasedIndex;
import br.pucrio.biobd.tap.agents.sgbd.models.PlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.algoritms.Algorithm;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael
 */
public class FunctionBasedIndexAlgorithm extends Algorithm {

    @Override
    public List<TuningAction> getTuningActions(SQL sql) {
        List<FunctionBasedIndex> lCandidates = new ArrayList<>();
        List<TuningAction> actions = new ArrayList<>();

        if (sql.getLastExecution() != null) {
            ArrayList<PlanOperation> seqScanOperations = sql.getLastExecution().getPlan().getOperationsByType(EnumPlanOperation.SeqScan);
            for (int i = 0; i < seqScanOperations.size(); i++) {
                PlanOperation seqScanOperation = seqScanOperations.get(i);
                actions.addAll(lCandidates);
            }
        }
        return actions;
    }

}
