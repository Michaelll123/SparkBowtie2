import org.apache.spark.SparkConf

import scala.collection.mutable.ArrayBuffer

object spark_bowtie2 {
  var conf=new SparkConf()
  def main(args: Array[String]): Unit = {

    if (args.length>1)
    {
      println(args.length)
      var options=new bowtie2_options
      var array=new ArrayBuffer[String]()
      for (arg<-args)
        array.append(arg)
       options.optionsTest(array)
      var bowtie2=new bowtie2_bowtie2
      bowtie2.runBowtie2(conf,options)
    }
    else
      println("error")

  }
}
