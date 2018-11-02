import java.io._
import java.util
import cn.edu.nwsuaf.sparkbowtie2
import org.apache.commons.logging.LogFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

class bowtie2_bowtie2 extends Serializable{

  @transient  var cf=new Configuration()
  var  LOG=LogFactory.getLog(this.getClass)
  var tmpDir:String=""
  var appId:String=""
  var appName:String=""
   var conf:SparkConf=null
  @transient var sc:SparkContext=null
  var options:bowtie2_options=null
  var CstartTime :Long=0
  def Init(conf:SparkConf,options:bowtie2_options): Unit ={
    if(options.inputPath.split("/")(options.inputPath.split("/").length - 1).contains("_"))
    this.conf = conf.setAppName("SparkBowtie2_" + options.inputPath.split("/")(options.inputPath.split("/").length - 1).split("_")(0)+"_"+options.partionNumber)
else
      this.conf = conf.setAppName("SparkBowtie2_" + options.inputPath.split("/")(options.inputPath.split("/").length - 1)+"_"+options.partionNumber)
    this.sc = new SparkContext(conf)
    this.cf = sc.hadoopConfiguration
    this.tmpDir = sc.getLocalProperty("spark.local.dir")

    if (this.tmpDir == null || (this.tmpDir == "null")) this.tmpDir = sc.hadoopConfiguration.get("hadoop.tmp.dir")

    if (this.tmpDir.startsWith("file:")) this.tmpDir = this.tmpDir.replaceFirst("file:", "")

    var tmpFileDir = new File(this.tmpDir)

    if (!tmpFileDir.isDirectory || !tmpFileDir.canWrite) this.tmpDir = "/tmp/"
    this.appId = sc.applicationId
    this.appName = sc.appName
    this.options=options

  }

  def handlePairdReads(sc :SparkContext, path1:String, path2 :String) :RDD[(Long, (String, String))]=
  {
    val rdd1=sc.textFile(path1).zipWithIndex().map(x =>
      ( Math.floor(x._2 / 4).toLong, ( x._1, x._2 % 4 ) )
    ).groupByKey.mapValues(x=>
    {
      var seqName:String = null
      var seq :String = null
      var qual :String = null
      var extraSeqname :String = null

      for (recordLine <- x) { // Keep in mind that records are sorted by key. This is, we have 4 available lines here
        val lineNum = recordLine._2
        val line = recordLine._1
        if (lineNum == 0) seqName = line
        else if (lineNum == 1) seq = line
        else if (lineNum == 2) extraSeqname = line
        else qual = line
      }

      // If everything went fine, we return the current record
      if (seqName != null && seq != null && qual != null && extraSeqname != null)
        String.format("%s\n%s\n%s\n%s\n", seqName, seq, extraSeqname, qual)
      else {
        System.err.println("Malformed record!")
        System.err.println(String.format("%s\n%s\n%s\n%s\n", seqName, seq, extraSeqname, qual))
        null
      }
    })
    val rdd2=sc.textFile(path2).zipWithIndex().map(x =>
      ( Math.floor(x._2 / 4).toLong, ( x._1, x._2 % 4 ) )
    ).groupByKey.mapValues(x=>
    {
      var seqName:String = null
      var seq :String = null
      var qual :String = null
      var extraSeqname :String = null

      for (recordLine <- x) { // Keep in mind that records are sorted by key. This is, we have 4 available lines here
        val lineNum = recordLine._2
        val line = recordLine._1
        if (lineNum == 0) seqName = line
        else if (lineNum == 1) seq = line
        else if (lineNum == 2) extraSeqname = line
        else qual = line
      }

      // If everything went fine, we return the current record
      if (seqName != null && seq != null && qual != null && extraSeqname != null)
        String.format("%s\n%s\n%s\n%s\n", seqName, seq, extraSeqname, qual)
      else {
        null
      }
    })
    var rdd= rdd1.join(rdd2)
    rdd1.unpersist()
    rdd2.unpersist()
    if(this.options.partionNumber!=0)
    rdd.repartition(this.options.partionNumber)
    else
      rdd

  }

  def runBowtie2(conf:SparkConf,options:bowtie2_options): Unit =
  {
    this.Init(conf,options)
    this.creatOutputFolder()
    this.mapPairdBowtie2(handlePairdReads(this.sc,options.inputPath,options.inputPath2).values)
  }
  def creatOutputFolder(): Unit ={

    try {
      val fs = FileSystem.get(this.cf)
      // Path variable
      val outputDir = new Path(this.options.outHdfsPath)
      // Directory creation
      if (!fs.exists(outputDir)) fs.mkdirs(outputDir)
      else {
        fs.delete(outputDir, true)
        fs.mkdirs(outputDir)
      }
      fs.close()
    } catch {
      case e: IOException =>
        LOG.error(e.toString)
        e.printStackTrace()
    }
  }
  def mapPairdBowtie2(readsRDD:RDD[(String,String)]): Unit ={

    val rdd=readsRDD.mapPartitionsWithIndex{
      (arg0,arg1)=> {
        var fastqFileName1: String = ""
        var fastqFileName2: String = ""

        if (this.tmpDir.lastIndexOf("/") == (this.tmpDir.length() - 1)) {
          fastqFileName1 = this.tmpDir + this.appId + "-RDD" + arg0 + "_1.fq"
          fastqFileName2 = this.tmpDir + this.appId + "-RDD" + arg0 + "_2.fq"

        }
        else {
          fastqFileName1 = this.tmpDir + "/" + this.appId + "-RDD" + arg0 + "_1.fq"
          fastqFileName2 = this.tmpDir + "/" + this.appId + "-RDD" + arg0 + "_2.fq"
        }
        var FastqFile1 = new File(fastqFileName1)
        var FastqFile2 = new File(fastqFileName2)
        var returnedValues=List[String]()
        //We write the data contained in this split into the two tmp files

          var fos1 = new FileOutputStream(FastqFile1)
          var fos2 = new FileOutputStream(FastqFile2)
          var bw1 = new BufferedWriter(new OutputStreamWriter(fos1))
          var bw2 = new BufferedWriter(new OutputStreamWriter(fos2))
        var startTime=System.currentTimeMillis()
          while (arg1.hasNext) {
            var newFastqRead = arg1.next()
            bw1.write(newFastqRead._1)
            bw1.newLine()
            bw2.write(newFastqRead._2)
            bw2.newLine()
          }
        var endTime = System.currentTimeMillis()
        println("写文件："+(endTime-startTime)/1000+"分钟")
          bw1.close()
          bw2.close()
          fos1.close()
          fos2.close()
        paridAlignment(arg0,fastqFileName1,fastqFileName2).iterator
      }

    }.collect()
    var endTime = System.currentTimeMillis()
    println(" copy用时："+(endTime-CstartTime)/ 1000 +" 分钟")
      var fs = FileSystem.get(this.cf)
      var finalHdfsOutputFile =new Path(this.options.outHdfsPath + "/"+this.appName+"_"+this.appId+".sam")
      val outputFinalStream = fs.create(finalHdfsOutputFile, true)
      // We iterate over the resulting files in HDFS and agregate them into only one file.
    var i = 0
    while ( {
      i < rdd.size
    }) {
      var br = new BufferedReader(new InputStreamReader(fs.open(new Path(rdd(i)))))
      var line = ""
      line = br.readLine
      while ( {
        line != null
      }) {
        if (i == 0 || !line.startsWith("@")) { //outputFinalStream.writeBytes(line+"\n");
          outputFinalStream.write((line + "\n").getBytes)
        }
        line = br.readLine
      }
      br.close()
      fs.delete(new Path(rdd(i)), true)
        i += 1
    }
    outputFinalStream.close

  }
  def paridAlignment(arg0:Int,fastq1:String,fastq2:String):List[String] =
  {
    var outputSamFileName=this.getOutputSamFilename(arg0)
    if (this.tmpDir.lastIndexOf("/") == this.tmpDir.length - 1)
      {

        this.options.outfilePath=this.tmpDir +""+outputSamFileName

      }
    else {
      this.options.outfilePath= this.tmpDir + "/" + outputSamFileName
    }
    var indexpath=this.options.indexPath
    var bowtie2=new sparkbowtie2()
    var op =new util.ArrayList[String]()
    for (str<-this.options.others)
      op.add(str)
    var result=bowtie2.runbowtie2(indexpath,fastq1,fastq2,this.options.outfilePath,op)
     CstartTime = System.currentTimeMillis()
    copyResults(outputSamFileName)

  }
  def getOutputSamFilename(readBatchID:Int): String = this.appName + "-" + this.appId + "-" + readBatchID + ".sam"
  def copyResults(outputSamFileName:String): List[String] ={



    val returnedValues = new util.ArrayList[String]
    val outputDir = this.options.outHdfsPath
    try { //if (outputDir.startsWith("hdfs")) {
      val conf = new Configuration
      val fs = FileSystem.get(conf)
      fs.copyFromLocalFile(new Path(this.options.outfilePath), new Path(outputDir))
    } catch {
      case e: IOException =>
        e.printStackTrace()
        this.LOG.error(e.toString)
    }


    val tmpSamFullFile= new File(this.options.outfilePath)
    tmpSamFullFile.delete()

    returnedValues.add(outputDir + "/" + outputSamFileName)
    var size=returnedValues.size()
    val array :List[String] = returnedValues.toArray(new Array[String](size)).toList

    array


  }

}
