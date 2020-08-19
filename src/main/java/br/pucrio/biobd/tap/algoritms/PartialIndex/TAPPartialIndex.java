/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms.PartialIndex;

import br.pucrio.biobd.tap.agents.libraries.Config;
import br.pucrio.biobd.tap.agents.libraries.Log;
import br.pucrio.biobd.tap.agents.sgbd.models.Column;
import br.pucrio.biobd.tap.agents.sgbd.models.EnumPlanOperation;
import br.pucrio.biobd.tap.agents.sgbd.models.PartialIndex;
import br.pucrio.biobd.tap.agents.sgbd.models.Restriction;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.agents.sgbd.models.implementors.TuningActionImplementor;
import br.pucrio.biobd.tap.algoritms.Algorithm;
import br.pucrio.biobd.tap.algoritms.ArrayListPermutation;
import br.pucrio.biobd.tap.algoritms.ExtractRestrictions;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rafael
 */
public class TAPPartialIndex extends Algorithm {

    @Override
    public List<TuningAction> getTuningActions(SQL sql) {
        List<TuningAction> allTuningActions = new ArrayList<>();
        if (sql.isValid()) {

            ExtractRestrictions extractRestrictions = new ExtractRestrictions();
            ArrayList<Restriction> joinRestrictions = extractRestrictions.getRestrictionsByOperationType(sql, EnumPlanOperation.Join);
            ArrayList<Restriction> seqScanRestrictions = extractRestrictions.getRestrictionsByOperationType(sql, EnumPlanOperation.SeqScan);

            if (seqScanRestrictions.size() <= Integer.valueOf(Config.getProperty("limitRestrictionsToMergePindex")) || Config.getProperty("turbo").equals("1")) {
                joinRestrictions.addAll(extractRestrictions.mergeRestrictions(joinRestrictions, seqScanRestrictions));
            }
            joinRestrictions.addAll(extractRestrictions.getRestrictionsByOperationType(sql, EnumPlanOperation.Aggregate));
            joinRestrictions = extractRestrictions.removeDuplicateRestrictions(joinRestrictions);
            seqScanRestrictions = extractRestrictions.removeDuplicateRestrictions(seqScanRestrictions);

            List<TuningAction> allPartialIndexes = this.generateComposedPartialIndexes(sql, joinRestrictions);
            allPartialIndexes.addAll(this.generateCrossPartialIndexes(sql, seqScanRestrictions, joinRestrictions));
            if (allPartialIndexes.size() <= Integer.valueOf(Config.getProperty("limitRestrictionsToMergePindex")) || Config.getProperty("turbo").equals("1")) {
                allPartialIndexes.addAll(this.generateCrossPartialIndexes(sql, joinRestrictions, joinRestrictions));
            }
            allTuningActions = this.removeDuplicateTuningActions(allPartialIndexes);
        }
        return allTuningActions;
    }

    private List<TuningAction> generateComposedPartialIndexes(SQL sql, ArrayList<Restriction> restrictions) {
        List<TuningAction> partialIndexes = new ArrayList<>();
        for (int i = 0; i < restrictions.size(); i++) {
            ArrayList<Restriction> restrictionsI = new ArrayList<>();
            restrictionsI.add(restrictions.get(i));
            for (int j = 1; j < restrictions.size(); j++) {
                if (!restrictionsI.contains(restrictions.get(j)) && i != j && this.isAValidCombination(restrictionsI, restrictions.get(j))) {
                    restrictionsI.add(restrictions.get(j));
                }
            }
            partialIndexes.addAll(this.generateAllPartialIndex(restrictionsI, sql));
        }
        return partialIndexes;
    }

    private ArrayList<TuningAction> generatePartialIndex(ArrayList<Column> columns, ArrayList<Object> restrictions, SQL sql) {
        ArrayList<TuningAction> partialIndexes = new ArrayList<>();
        PartialIndex partialIndex = new PartialIndex();
        for (Object restriction : restrictions) {
            partialIndex.addRestriction((Restriction) restriction);
        }
        partialIndex.setStatus("H");
        partialIndex.addSQL(sql);

        partialIndex.setIndexedColumn(columns);
        partialIndexes.add(partialIndex);
        return partialIndexes;
    }

    private boolean isAValidCombination(ArrayList<Restriction> restrictionsI, Restriction restriction) {
        if (!restrictionsI.get(0).getColumn().getTableName().equals(restriction.getColumn().getTableName())) {
            return false;
        }
        if (!this.isAValidRestrictionForPartialIndex(restriction)) {
            return false;
        }
        for (Restriction restrictionItem : restrictionsI) {
            if (restrictionItem.equals(restriction) || (restrictionItem.getColumn().equals(restriction.getColumn()) && restrictionItem.getValue().equals(restriction.getValue()))) {
                return false;
            }
        }
        return true;
    }

    private boolean isAValidRestrictionForPartialIndex(Restriction restriction) {
        return TuningActionImplementor.isAValidPartialIndexRestriction(restriction);
    }

    public boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    private ArrayList<TuningAction> generateAllPartialIndex(ArrayList<Restriction> restrictions, SQL sql) {
        ArrayList<TuningAction> partialIndexes = new ArrayList<>();
        ArrayList<Object> itemList = new ArrayList<>();
        itemList.addAll(restrictions);
        if (itemList.size() > 1) {
            for (ArrayList<Object> listRestrictions : ArrayListPermutation.rafaelsPermutationLimited(itemList)) {
                partialIndexes.addAll(this.generatePartialIndex(this.extractIndexedColumns(listRestrictions), listRestrictions, sql));
            }
        } else {
            partialIndexes.addAll(this.generatePartialIndex(this.extractIndexedColumns(itemList), itemList, sql));
        }
        return partialIndexes;
    }

    private void printAllRestrictions(ArrayList<Restriction> restrictions) {
        Log.msg("");
        for (Restriction restriction : restrictions) {
            restriction.print();
        }
        Log.msg("");
    }

    private ArrayList<Column> extractIndexedColumns(ArrayList<Object> listRestrictions) {
        ArrayList<Column> indexedColumns = new ArrayList<>();
        for (Object listRestriction : listRestrictions) {
            Column column = ((Restriction) listRestriction).getColumn();
            if (!indexedColumns.contains(column)) {
                indexedColumns.add(column);
            }
        }
        return indexedColumns;
    }

    private List<TuningAction> generateCrossPartialIndexes(SQL sql, ArrayList<Restriction> seqScanRestrictions, ArrayList<Restriction> hashRestrictions) {
        List<TuningAction> partialIndexes = new ArrayList<>();
        for (int i = 0; i < hashRestrictions.size(); i++) {
            ArrayList<Restriction> restrictionsI = new ArrayList<>();
            restrictionsI.add(hashRestrictions.get(i));
            for (int j = 1; j < hashRestrictions.size(); j++) {
                if (i != j && this.isAValidCombination(restrictionsI, hashRestrictions.get(j))) {
                    restrictionsI.add(hashRestrictions.get(j));
                }
            }
            partialIndexes.addAll(this.generateAllCrossPartialIndex(hashRestrictions, seqScanRestrictions, sql));
        }
        return partialIndexes;
    }

    private ArrayList<TuningAction> generateAllCrossPartialIndex(ArrayList<Restriction> restrictions, ArrayList<Restriction> seqScanRestrictions, SQL sql) {
        ArrayList<TuningAction> partialIndexes = new ArrayList<>();
        ArrayList<Object> itemList = new ArrayList<>();
        itemList.addAll(restrictions);
        if (itemList.size() > 1) {
            for (ArrayList<Object> listRestrictions : ArrayListPermutation.rafaelsPermutationLimited(itemList)) {
                for (Restriction seqScanRestriction : seqScanRestrictions) {
                    ArrayList<Column> columnList = new ArrayList<>();
                    columnList.add(seqScanRestriction.getColumn());
                    partialIndexes.addAll(this.generatePartialIndex(columnList, listRestrictions, sql));
                    columnList = new ArrayList<>();
                    if (seqScanRestriction.getValue() instanceof Column) {
                        columnList.add((Column) seqScanRestriction.getValue());
                        partialIndexes.addAll(this.generatePartialIndex(columnList, listRestrictions, sql));
                    }
                }
            }
        } else {
            for (Restriction seqScanRestriction : seqScanRestrictions) {
                ArrayList<Column> columnList = new ArrayList<>();
                columnList.add(seqScanRestriction.getColumn());
                partialIndexes.addAll(this.generatePartialIndex(columnList, itemList, sql));
            }
        }
        return partialIndexes;
    }

}
