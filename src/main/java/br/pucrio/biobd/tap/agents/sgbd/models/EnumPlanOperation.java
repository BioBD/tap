/*
 * Tuning Action Plataform - TAP
 * BioBD Lab - PUC-Rio  *
 * Rafael Pereira - rpoliveira@inf.puc-rio.br *
 */
package br.pucrio.biobd.tap.agents.sgbd.models;

/**
 *
 * @author rpoat
 */
public enum EnumPlanOperation {
    Join,
    HashJoin,
    MergeJoin,
    SeqScan,
    Aggregate,
    Other
}
