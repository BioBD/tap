/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms.Index;

import br.pucrio.biobd.tap.agents.sgbd.models.EnumPlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.Filter;
import br.pucrio.biobd.tap.agents.sgbd.models.Index;
import br.pucrio.biobd.tap.agents.sgbd.models.PlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.algoritms.Algorithm;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author josemaria_thales
 */
public class IHSTIS_CI extends Algorithm {

    @Override
    public List<TuningAction> getTuningActions(SQL sql) {
        List<Index> lCandidates = new ArrayList<>();
        List<TuningAction> actions = new ArrayList<>();

        if (sql.getLastExecution() != null) {
            ArrayList<PlanOperation> seqScanOperations = sql.getOperationsByType(EnumPlanOperation.SeqScan);
            seqScanOperations.addAll(sql.getOperationsByType(EnumPlanOperation.Join));
            seqScanOperations.addAll(sql.getOperationsByType(EnumPlanOperation.Aggregate));

            for (int i = 0; i < seqScanOperations.size(); i++) {
                PlanOperation seqScanOperation = seqScanOperations.get(i);
                Index composedIndexP = this.createcomposedPrimaryIndex(seqScanOperation);
                Index composedIndexS = this.createcomposedSecondaryIndex(seqScanOperation);

                ArrayList<Filter> filterColumns = seqScanOperation.getFilterColumns(sql);
                for (int j = 0; j < filterColumns.size(); j++) {
                    Filter filterAux = filterColumns.get(j);
                    this.ajustComposedIndexs(composedIndexP, composedIndexS, filterAux);
                    Index indexAuxP = this.createCandidateIndex(seqScanOperation, filterAux, "P");
                    lCandidates.add(indexAuxP);
                    Index indexAuxS = this.createCandidateIndex(seqScanOperation, filterAux, "S");
                    lCandidates.add(indexAuxS);
                }
                if (filterColumns.size() > 1) {
                    lCandidates.add(composedIndexP);
                    lCandidates.add(composedIndexS);
                }
            }
            actions.addAll(lCandidates);
        }
        for (TuningAction action : actions) {
            action.addSQL(sql);
        }
        return actions;
    }

    private Index createcomposedPrimaryIndex(PlanOperation SeqScanOperation) {
        return this.createComposedIndex(SeqScanOperation, "P");
    }

    private Index createcomposedSecondaryIndex(PlanOperation SeqScanOperation) {
        return this.createComposedIndex(SeqScanOperation, "S");
    }

    private Index createComposedIndex(PlanOperation seqScanOperation, String type) {
        Index composedIndex = new Index();
        composedIndex.setIndexType(type);
        composedIndex.setNumberOfRows(seqScanOperation.getNumberOfRowsOfFullScan());
        return composedIndex;
    }

    private void ajustComposedIndexs(Index composedIndexP, Index composedIndexS, Filter filterAux) {
        composedIndexP.addColumn(filterAux);
        composedIndexS.addColumn(filterAux);

        if ((composedIndexP.getFilterType() == null) || (!composedIndexP.getFilterType().equals("equi"))) {
            composedIndexP.setFilterType(filterAux.getFilterType());
            composedIndexS.setFilterType(filterAux.getFilterType());
        }
        composedIndexP.setHasFilter(true);
        composedIndexS.setHasFilter(true);
    }

    private Index createCandidateIndex(PlanOperation seqScanOperation, Filter filterAux, String type) {
        Index newCandidateIndex = new Index();
        newCandidateIndex.setIndexType(type);
        newCandidateIndex.addColumn(filterAux);
        newCandidateIndex.setHasFilter(true);
        newCandidateIndex.setFilterType(filterAux.getFilterType());
        newCandidateIndex.setNumberOfRows(seqScanOperation.getNumberOfRowsOfFullScan());
        return newCandidateIndex;
    }

}
