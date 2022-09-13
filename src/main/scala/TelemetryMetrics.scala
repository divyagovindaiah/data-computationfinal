/**
 * total number of completed contents by user
 * total number of distinct channel
 */

import java.io.{
  BufferedInputStream,
  BufferedReader,
  File,
  FileInputStream,
  InputStreamReader,
  PrintWriter
}
import java.util.zip.GZIPInputStream
import com.google.gson.Gson
import com.typesafe.config.{ConfigFactory}

object telemetryMetrics extends App {
  val gson = new Gson()
  // $COVERAGE-OFF$
  def main(): Unit = {

    val applicationConf = ConfigFactory.load("application.conf")
    val sourceFile = applicationConf.getString("app.sourceFile")
    val outputFile = applicationConf.getString("app.output.totalContentOutFile")
    val data = process(sourceFile,outputFile)
    toFile(data,outputFile)
  }
  // $COVERAGE-ON$

  def process(sourcefile: String, outputfile: String): OutputData = {
    val telemetryData = readFile(sourcefile)
    val distnctchannel = getUniqueChannel(telemetryData)
    val contentcompleted = getContentProgress(telemetryData)
    val finalData = OutputData(
      contentcompleted.map(r => (ProgressData(r._1, r._2))).toArray,
      distnctchannel.map(r => (channel(Option(r._1).getOrElse("Unknown"), r._2))).toArray)
    return finalData
  }
  /**
   * Loading Gzip file convert into buffer reader then in the form of list
   * @return: returns list of strings
   */
  def readFile(sourceFile: String): List[TelemetryStructure] = {
    val gzipData = new GZIPInputStream(new BufferedInputStream(new FileInputStream(sourceFile)))
    val reader = new BufferedReader(new InputStreamReader(gzipData))
    Iterator.continually(
      lineToTelemetry(reader.readLine())
    ).takeWhile(_ != null).toList
  }
  /**
   * Takes in line from stream data in BufferedReader and returns the json parsed class object
   * @param line : Json object line from file stream
   * @return TelemetryData
   */
  def lineToTelemetry(line: String): TelemetryStructure = {
    val gson = new Gson()
    gson.fromJson(line, classOf[TelemetryStructure])
  }

  /**
   * This function can check the progress of content is 100.0 or not
   * Then returns number of content completed by a user
   * @param result   : List of Telemetry Objects
   * @param progress : compare the progress value
   * @param userId   : get the count by specific userId
   * @return: returns count of completed content by a user
   */
  def countCompleted(result: List[TelemetryStructure], progress: Double, userId: String): Int =
    result.count(x => x.getProgress == progress)

  /**
   * it can  group the content those are the progress 100.0
   * @param result : contains list of telemetryObjects
   * @return: count the number of completed contents
   */

  def getContentProgress(result: List[TelemetryStructure]) = {
    val seperate = result.filter (x => x.eid == "END")
    val finalRes = seperate.groupBy(record => (record.getUserId)).map { f =>
      f._1 -> countCompleted(f._2, 100, f._1)
    }
finalRes
}
  /**
   * it can group the channalls are distinct
   * @param result : contains List of telemetry objects
   * @return: find out the number of disctinctchannnel
   */
  def getUniqueChannel(result: List[TelemetryStructure]) = {
    val metrics = result.groupBy(f => f.getChannelId).map(f => (f._1, f._2.size))

metrics
}
  /**
   * it can writes the output to a file
   * @param data : it can takes outputdata
   */
  def toFile(data:OutputData,outputFilePath:String) = {
    // $COVERAGE-OFF$

    val jsonData = gson.toJson(data)
    val fileWriter = new PrintWriter(
     new File(outputFilePath)
   )
   try fileWriter.write(jsonData) finally fileWriter.close()
 }
  main()
  // $COVERAGE-ON$


}



