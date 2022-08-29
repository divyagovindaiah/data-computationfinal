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
  val applicationConf = ConfigFactory.load("config.conf")
  val sourceFile = applicationConf.getString("app.sourceFile")
  val outputFile = applicationConf.getString("app.output.totalContentOutFile")
  val metricsOutFile = applicationConf.getString("app.output.metricsOutFile")

  /**
   * Loading Gzip file convert into buffer reader then in the form of list
   * @return: returns list of strings
   */
  def readFile(sourceFile: String): BufferedReader = {
    val gzipData = new GZIPInputStream(new BufferedInputStream(new FileInputStream(sourceFile)))
    val reader = new BufferedReader(new InputStreamReader(gzipData))
    reader
  }
 /**
  * Takes in line from stream data in BufferedReader and returns the json parsed class object
  * @param line: Json object line from file stream
  * @return TelemetryData
  */
  def lineToTelemetry(line: String): TelemetryStructure = {
    val gson = new Gson()
    gson.fromJson(line, classOf[TelemetryStructure])
  }

  /**
   * Takes in stream data in BufferedReader and returns the list of objects in the stream
   * @param reader: BufferedReader which has all the data of file
   * @return Iterator[TelemetryStructure]
  */
  def process(reader: BufferedReader): List[TelemetryStructure] =
    Iterator.continually(
      lineToTelemetry(reader.readLine())
    ).takeWhile(_ != null).toList

  /**
   * This function can check the progress of content is 100.0 or not
   * Then returns number of content completed by a user
   * @param result   : List of Telemetry Objects
   * @param progress : compare the progress value
   * @param userId   : get the count by specific userId
   * @return: returns count of completed content by a user
   */
  def countCompleted(result: List[TelemetryStructure], progress: Double, userId: String): Int =
    result.count(x =>
      x.getProgress == progress &&
        x.getUserId == userId)

  /**
   * This function can  count the number of distcint channel
   * @channel : get the count of chaneelId
   */

  def distinctChannels(result1: List[TelemetryStructure], channel: String): Int = {
    result1.count(x => x.getChannelId == channel)
  }

  /**
   * it can  group the content those are the progress 100.0
   * @param result : contains list of telemetryObjects
   * @return: count the number of completed contents
   */

  def getContentProgress(result: List[TelemetryStructure]): Map[String, Int] = {
    result.groupBy(record => (record.getUserId)).map {
      case (userId, logObjects) => userId ->
        countCompleted(logObjects, 100.0, userId)
    }

  }

  /**
   * it can group the channalls are distinct
   * @param result: contains List of telemetry objects
   * @return: find out the number of disctinctchannnel
   */

  def getUniqueChannel(result: List[TelemetryStructure]): Map[String, Int] = {
    val metrics = result.groupBy(f => f.getChannelId).map(f => (f._1, f._2.size))
    metrics
  }

  /**
   * it can writes the output to a file
   *@param data : it can takes outputdata
   */

  def toFile(data: OutputData) = {
    val jsonData = gson.toJson(data)
    val fileWriter = new PrintWriter(
      new File(outputFile)
    )
    try fileWriter.write(jsonData) finally fileWriter.close()
  }

  /**
   *This function is used to write the obtained metrics to a new json file
   *@param data it can takes outputData1
   */

  def toFile1(data: OutputData1) = {
    val jsonData = gson.toJson(data)
    val fileWriter = new PrintWriter(
      new File(metricsOutFile)

    )
    try fileWriter.write(jsonData) finally fileWriter.close()
  }

  val readData = readFile("/home/sanctum/Downloads/project.json.gz")
  val telemetryData = process(readData)
  val completedContent = getContentProgress(telemetryData)
  val distinctChannels = getUniqueChannel(telemetryData)
  val finalData = new OutputData(
    completedContent.map(
      r => new ProgressData(r._1, r._2).asInstanceOf[ProgressData]
    ).toList.toArray)
  toFile(finalData)
  distinctChannels.map(f => println("data", f))
  val finalData1 = new OutputData1(
    distinctChannels.map(
      r =>  new channel(r._1,r._2).asInstanceOf[channel]
    ).toArray)
  toFile1(finalData1)
}


