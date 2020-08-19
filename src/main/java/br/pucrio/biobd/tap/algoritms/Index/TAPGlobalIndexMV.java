/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms.Index;

import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.sgbd.models.EnumPlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.Index;
import br.pucrio.biobd.tap.agents.sgbd.models.Restriction;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.algoritms.Algorithm;
import br.pucrio.biobd.tap.algoritms.ArrayListPermutation;
import br.pucrio.biobd.tap.algoritms.ExtractRestrictions;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rpoat
 */
public class TAPGlobalIndexMV extends Algorithm {

    @Override
    public List<TuningAction> getTuningActions(SQL sql) {
        ExtractRestrictions extractRestrictions = new ExtractRestrictions();
        ArrayList<Restriction> joinRestrictions = extractRestrictions.getRestrictionsByOperationType(sql, EnumPlanOperation.Join);
        ArrayList<Restriction> seqScanRestrictions = extractRestrictions.getRestrictionsByOperationType(sql, EnumPlanOperation.SeqScan);
        ArrayList<Restriction> mergedRestrictions = extractRestrictions.mergeRestrictions(joinRestrictions, seqScanRestrictions);
        IHSTIS ihstis = new IHSTIS();
        List<TuningAction> allCandidateIndexes = ihstis.getTuningActions(sql);
        allCandidateIndexes.addAll(this.generateComposedIndex(mergedRestrictions));

        return this.removeDuplicateTuningActions(allCandidateIndexes);
    }

    private List<TuningAction> generateComposedIndex(List<Restriction> allRestrictions) {
        List<TuningAction> indexes = new ArrayList<>();
        ArrayList<Object> restrictions = new ArrayList<>();
        for (Restriction restriction : allRestrictions) {
            if (!restrictions.contains(restriction)) {
                restrictions.add(restriction);
            }
        }

        ArrayList<ArrayList<Object>> listRestrictions = ArrayListPermutation.rafaelsPermutationLimited(restrictions);

        for (ArrayList<Object> listRestriction : listRestrictions) {
            indexes.add(createCandidateIndex(listRestriction));
        }
        return indexes;
    }

    private Index createCandidateIndex(ArrayList<Object> restrictions) {
        Index newCandidateIndex = new Index();
        newCandidateIndex.setIndexType("S");
        for (Object restriction : restrictions) {
            newCandidateIndex.addColumn(((Restriction) restriction).getColumn());
            newCandidateIndex.setNumberOfRows(((Restriction) restriction).getNumRows());
        }
        newCandidateIndex.setHasFilter(true);
        newCandidateIndex.setFilterType("equi");
        return newCandidateIndex;
    }

}
