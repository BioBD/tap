/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.algoritms.PartialIndex;

import br.pucrio.biobd.tap.agents.sgbd.models.MaterializedView;
import br.pucrio.biobd.tap.agents.sgbd.models.PartialIndex;
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
public class PARTIAL_INDEX_OVER_MV_GLOBAL_TUNING extends GlobalTuningAlgorithm {

    @Override
    public List<TuningAction> getTuningActions(TuningAction action) {
        List<TuningAction> indexOverMV = new ArrayList<>();
        for (SQL sqlCreateMV : action.getSqlList()) {
            sqlCreateMV.setTuningAction(true);
            PartialIndexCreate partialIndexAlg = new PartialIndexCreate();
            indexOverMV = partialIndexAlg.getTuningActions(sqlCreateMV);
            for (TuningAction tuningAction : indexOverMV) {
                this.replaceTableByMaterializedView((PartialIndex) tuningAction, (MaterializedView) action);
                tuningAction.setParentTuningAction(action);
                tuningAction.addSQL(action.getLastSql());
            }
        }
        return indexOverMV;
    }

    private void replaceTableByMaterializedView(PartialIndex index, MaterializedView materializedView) {
        Table mvTable = this.createTableByMV(materializedView);
        index.setGlobalTable(mvTable);
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
