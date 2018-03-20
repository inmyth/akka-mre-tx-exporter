package com.mbcu.mre.tx.exporter.models

import play.api.libs.json._
import play.api.libs.functional.syntax._


//TransID|TransType|FromAcct|FromSeq|LedgerSeq|Status|RawTxn|TxnMeta
// CEAF976B2BF92F2C51C4D8AE34085CFC3E435A23690F3306B6BD517773C91931|TrustSet|rRXPAamu7uBESCfQBvNxSVNJvKSqbsmGB|13|3713038|V||

object Transaction {

  implicit val jsonFormat: OFormat[Transaction] = Json.format[Transaction]

  object Implicits {
    implicit val transactionWrites: Writes[Transaction] {
      def writes(a: Transaction): JsValue} = new Writes[Transaction] {
      def writes(a: Transaction): JsValue = Json.obj(
        "TransId" -> a.TransId,
        "TransType" -> a.TransType,
        "FromAcct" -> a.FromAcct,
        "FromSeq" -> a.FromSeq,
        "LedgerSeq" -> a.LedgerSeq,
        "Status" -> a.Status,
        "RawTxn" -> a.RawTxn,
        "TxnMeta" -> a.TxnMeta
      )
    }

    implicit val transactionReads: Reads[Transaction] = (
      (JsPath \ "TransId").read[String] and
        (JsPath \ "TransType").read[String] and
        (JsPath \ "FromAcct").read[String] and
        (JsPath \ "FromSeq").read[Long] and
        (JsPath \ "LedgerSeq").read[Long] and
        (JsPath \ "Status").read[Int] and
        (JsPath \ "RawTxn").read[String] and
        (JsPath \ "TxnMeta").read[String]
      ) (Transaction.apply _)
  }

}
case class Transaction (
                       TransId : String,
                       TransType: String,
                       FromAcct : String,
                       FromSeq : Long,
                       LedgerSeq : Long,
                       Status : Int,
                       RawTxn : String,
                       TxnMeta : String
                       )
