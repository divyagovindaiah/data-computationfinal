import com.typesafe.config.ConfigFactory
import org.scalatest.flatspec.AnyFlatSpec

class TelemetryMetricsTest extends AnyFlatSpec {
  val applicationConf = ConfigFactory.load("config.conf")
  val sourceFile = applicationConf.getString("app.sourceFile")
  val readFile= telemetryMetrics.readFile(sourceFile)
  it should "return the total content progress" in {
     assert(telemetryMetrics.getContentProgress(readFile).size == 5)
  }

  it should "return count the total number of channels" in {
    val channelInfo = telemetryMetrics.getUniqueChannel(readFile)
    assert(channelInfo.size == 6)
  }
}







