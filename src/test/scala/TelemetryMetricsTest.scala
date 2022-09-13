import com.typesafe.config.ConfigFactory
import org.scalatest.flatspec.AnyFlatSpec
import java.io.File

class TelemetryMetricsTest extends AnyFlatSpec {
  val applicationConf = ConfigFactory.load("application.conf")
  val sourceFile = applicationConf.getString("app.sourceFile")
  val readFile = telemetryMetrics.readFile(sourceFile)
  val outputFile = applicationConf.getString("app.output.totalContentOutFile")

  it should "return the total content progress" in {
    assert(telemetryMetrics.getContentProgress(readFile).size == 5)
  }

  it should "return count the total number of channels" in {
    val channelInfo = telemetryMetrics.getUniqueChannel(readFile)
    assert(channelInfo.size == 6)
  }

 it should "details of progressData, channale" in {
    telemetryMetrics.process(sourceFile,outputFile)
  }
  it should "to check output return" in {
    assert((new File(outputFile).exists() == true))

  }

}











