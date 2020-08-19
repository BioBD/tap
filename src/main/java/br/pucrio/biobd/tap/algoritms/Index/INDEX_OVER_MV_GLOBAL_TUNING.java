/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms.Index;

import br.pucrio.biobd.tap.agents.sgbd.models.Index;
import br.pucrio.biobd.tap.agents.sgbd.models.MaterializedView;
import br.pucrio.biobd.tap.agents.sgbd.models.SQL;
import br.pucrio.biobd.tap.agents.sgbd.models.Table;
import br.pucrio.biobd.tap.agents.sgbd.models.TuningAction;
import br.pucrio.biobd.tap.algoritms.GlobalTuningAlgorithm;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rpoat
 */
public class INDEX_OVER_MV_GLOBAL_TUNING extends GlobalTuningAlgorithm {

    @Override
    public List<TuningAction> getTuningActions(TuningAction action) {
        List<TuningAction> allIndexes = new ArrayList<>();
        for (SQL sqlCreateMV : action.getSqlList()) {
            sqlCreateMV.setTuningAction(true);
            TAPGlobalIndexMV globalIndex = new TAPGlobalIndexMV();
            allIndexes.addAll(globalIndex.getTuningActions(sqlCreateMV));

            for (TuningAction tuningAction : allIndexes) {
                this.replaceTableByMaterializedView((Index) tuningAction, (MaterializedView) action);
                tuningAction.setParentTuningAction(action);
                tuningAction.addSQL(action.getLastSql());
            }
        }
        return allIndexes;
    }

    private void replaceTableByMaterializedView(Index index, MaterializedView materializedView) {
        Table mvTable = this.createTableByMV(materializedView);
        index.setTable(mvTable);
    }

    private Table createTableByMV(MaterializedView materializedView) {
        Table currentTable = new Table();
        currentTable.setSchema(materializedView.getSchema());
        currentTable.setName(materializedView.getName());
        currentTable.setNumberRows(materializedView.getNumRow());
        currentTable.setNumberPages(materializedView.getHypoNumPages());
        currentTable.setFields(this.getColumns(materializedView, currentTable));
        return currentTable;
    }

}
