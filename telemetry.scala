package com.microsoft.spark.example
import org.apache.spark.sql.{SparkSession, functions}
import org.apache.spark.sql._
import org.apache.spark.sql.functions.{col, collect_list, expr, map_from_entries, struct}


case class TelemetryStructure(fields: Array[Telemetry]) {

}

case class Telemetry(
                        eid: String,
                        progress: ProgressData,
                        edata: EData,
                        actor: Actor,
                        context: ContextDetails,
                        `object`: ObjectDetails)

case class Actor(
                  `type`: String,
                  id: String
                )
case class EData(
                  state: String,
                  summary: Seq[Summary],
                  props: List[String],
                  `type`: String,
                  pageid: String
                )
case class Summary (
                     progress: Option[Int],
                     totallength: Option[Int],
                     visitedlength: Option[Int],
                     visitedcontentend: Option[Boolean],
                     totalseekedlength: Option[Int],
                     endpageseen: Option[Boolean]
                   )

case class ProgressData(
                         userId: String,
                         completed: Int
                       )

case class ObjectDetails(
                          id: String,
                          `type`: String
                        )
case class ContextDetails(
                           sid: String,
                           did: String,
                           channel: String
                         )

  case class Channel(
                      id: String,
                      count: Int
                    )
  
object telemetryMetrics extends  App {

  def main(): Unit = {
    val sparkSession = SparkSession.builder().config("spark.master", "local[*]").getOrCreate()
    computeProgress(sparkSession)

  }
  def computeProgress(spark: SparkSession): Unit = {
    val schema = Encoders.product[Telemetry].schema
    val DataSet = spark.read.schema(schema).option("inferSchema", "true").json("/home/stpl/IdeaProjects/scala-sparkoperation/src/main/resources/Telemetrys.json")
    DataSet.show()

    /**
     * total number of distinct channel
     */
    val DistinctChannel = DataSet.groupBy(("context.channel")).count()
    DistinctChannel.show()
 DistinctChannel.write.format("json").save("src/main/resources/data.json")

    /**
     * total number of completed content by user
     */
    val  CountCompleted = DataSet.filter(DataSet("eid") === "END").groupBy(DataSet("actor.id")).agg(functions.count("actor"))
    CountCompleted.show()
  }
    main()



}
