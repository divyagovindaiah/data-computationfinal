import java.util.HashMap
import java.util.List

case class TelemetryStructure(
  eid: String,
  progress:ProgressData,
  context: ContextDetails,
  edata: EData,
  actor: Actor,
  `object`: ObjectDetails) {


  def getUserId = actor.id
  def getChannelId: String = context.channel
  def getProgress:Double = {
    var c = 0.0
    if (edata.summary.isInstanceOf[List[HashMap[String, String]]]) {
      edata.summary.forEach(x =>
        if (x.containsKey("progress"))
          c += x.get("progress").toDouble
      )
    }
    c
  }

  def getContentId: String = {
    if (`object`.isInstanceOf[ObjectDetails])
      `object`.id
    else "None"
  }
}

case class ContextDetails(
  sid: String,
  did: String,
  pdata: PData,
  channel: String
)

case class ProgressData(
  userId: String,
  completed: Int
)

case class Actor(
  `type`: String,
  id: String
)

case class PData(
  id: String,
  pid: String,
  ver: String
)

case class EData(
  state: String,
  props: List[String],
  summary: List[HashMap[String,String]]
)

case class Rollup(
  l1: String,
  l2: String
)

case class ObjectDetails(
  id: String,
  `type`: String,
  rollup: Rollup
)

case class channel(
  id: String,
  count: Int
)

case class OutputData(
  inProgressContent: Array[ProgressData],
  uniqueChannel: Array[channel]

)

