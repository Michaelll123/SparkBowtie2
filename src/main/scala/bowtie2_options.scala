
import scala.collection.mutable.ArrayBuffer


class bowtie2_options extends Serializable{
   var indexPath :String =""
   var tmpPath :String=""
   var outfilePath :String=""
   var outHdfsPath :String=""
   var inputPath :String =""
   var inputPath2 :String =""
   var partionNumber :Int =0
  var others =new ArrayBuffer[String]()


  //判断shell获取的参数
  def optionsTest(args :ArrayBuffer[String]): Unit =
  {
    for( arg <- args)
    {
      arg match {
        case "-x"=>
        {
          this.indexPath=args(args.indexOf("-x")+1)
          println("index文件路径: "+this.indexPath)
        }
        case "-1"=>
        {
          this.inputPath=args(args.indexOf("-1")+1)
          println("fastq1 文件路径: "+this.inputPath)
        }
        case "-2"=> {
          this.inputPath2=args(args.indexOf("-2")+1)
          println("fastq2 文件路径: "+this.inputPath2)
        }
        case "-S" =>
        {
          this.outHdfsPath=args(args.indexOf("-S")+1)
          println("输出到hdfs文件夹路径: "+this.outHdfsPath)
        }
        case "-P"=>
          {
              this.partionNumber=args(args.indexOf("-P")+1).toInt
            println("设置分区数 : "+this.partionNumber)
          }
       //性能参数
//      -o/--offrate <int> 无视index的offrate值, 以<int>取代之. Index默认的<int> 值为5. <int>值必须大于index的offrate值, 同时<int>越大, 耗时越长，耗内存越少.
//      -p/--threads NTHREADS 设置线程数. Default: 1
//      --reorder 多线程运算时, 比对结果在顺序上会和文件中reads的顺序不一致, 使用该选项, 则使其一致.
//      --mm 使用内存定位的I/O来载入index, 而不是常规的文件I/O. 从而使多个bowtie程序共用内存中同样的index, 节约内存消耗.
        case "-p"  =>
          {
              this.others.append("-p")
             this.others.append(args(args.indexOf("-p")+1))
            println("设置模拟线程数 : "+args(args.indexOf("-p")+1))
          }
        case "--threads"  =>
        {
          this.others.append("--threads")
          this.others.append(args(args.indexOf("--threads")+1))
        }
        case "--reorder"  =>
        {
          this.others.append("--reorder")
        }
        case "--mm"  =>
        {
          this.others.append("--mm")
        }
        case "-o"  =>
        {
          this.others.append("-o")
          this.others.append(args(args.indexOf("-o")+1))
        }
        case "--offrate"  =>
        {
          this.others.append("--offrate")
          this.others.append(args(args.indexOf("--offrate")+1))
        }
        //sam参数
//      --no-unal 不记录没比对上的reads.
//      --no-hd 不记录SAM header lines (以@开头).
//      --no-sq 不记录@SQ的SAM header lines.
//      --rg-id <text> 设定read group Id到<text>.
//      --rg <text> 增加<text>作为一行@RG.

        case "--no-unal"=>
          {
            this.others.append("--no-unal")
          }
        case "--no-hd"=>
        {
          this.others.append("--no-hd")
        }
        case "--no-sq"=>
        {
          this.others.append("--no-sq")
        }
        case "--rg-id"=>
        {
          this.others.append("--rg-id")
          this.others.append(args(args.indexOf("--rg-id")+1))
        }
        case "--rg"=>
        {
          this.others.append("--rg")
          this.others.append(args(args.indexOf("--rg")+1))
        }
        //比对参数
//          -N <int> 进行种子比对时允许的mismatch数. 可以设为0或者1. Default: 0.
//          -L <int> 设定种子的长度.

        case "-N"=>
        {
          this.others.append("-N")
          this.others.append(args(args.indexOf("-N")+1))
        }
        case "-L"=>
        {
          this.others.append("-L")
          this.others.append(args(args.indexOf("-L")+1))
        }
        //得分罚分参数
//          --ma <int> 设定匹配得分. --local模式下每个read上碱基和参考序列上碱基匹配, 则加<int>分. 在—end-to-end模式中无效. Default: 2.
//          --mp MX,MN 设定错配罚分.
//          --np <int> 当匹配位点中read, reference上有不确定碱基(比如N)时所设定的罚分值.Default: 1.
//          --rdg <int1>,<int2> 设置在read上打开gap 罚分<int1>, 延长gap罚分<int2>.Default: 5, 3.
//          --rfg <int1>,<int2> 设置在reference上打开gap 罚分<int1>, 延长gap罚分<int2>. Default: 5, 3.
//          --score-min <func> 设定成为有效比对的最小分值. 在—end-to-end模式下默认值为:L,-0.6,-0.6; 在--local模式下默认值为: G,20,8.
        case "--ma"=>
        {
          this.others.append("--ma")
          this.others.append(args(args.indexOf("--ma")+1))
        } case "--np"=>
        {
          this.others.append("--np")
          this.others.append(args(args.indexOf("--np")+1))
        } case "--rdg"=>
        {
          this.others.append("--rdg")
          this.others.append(args(args.indexOf("--rdg")+1))
        }
        case "--rfg"=>
        {
          this.others.append("--rfg")
          this.others.append(args(args.indexOf("--rfg")+1))
        }
        case "--score-min"=>
          {
            this.others.append("--score-min")
            this.others.append(args(args.indexOf("--score-min")+1))
          }
        //输入参数
//      --phred33 输入的碱基质量等于ASCII码值加上33. 在最近的illumina pipiline中得以运用。
//      --phred64 输入的碱基质量等于ASCII码值加上64.
//      -5/--trim5 <int> 剪掉5'端<int>长度的碱基，再用于比对。(default: 0).
//      -3/--trim3 <int> 剪掉3'端<int>长度的碱基，再用于比对。(default: 0).
//      --solexa-quals 将Solexa的碱基质量转换为Phred。在老的GA Pipeline版本中得以运用。Default: off.
        case "-5"=>
        {
          this.others.append("-5")
          this.others.append(args(args.indexOf("-5")+1))
        }case "--trim5"=>
        {
          this.others.append("--trim5")
          this.others.append(args(args.indexOf("--trim5")+1))
        }case "-3"=>
        {
          this.others.append("-3")
          this.others.append(args(args.indexOf("-3")+1))
        }case "--trim3"=>
        {
          this.others.append("--trim3")
          this.others.append(args(args.indexOf("--trim3")+1))
        }
        case "--phred33"=>
        {
          this.others.append("--phred33")
        }
        case "--phred64"=>
        {
          this.others.append("--phred64")
        }
        case "--solexa-quals"=>
        {
          this.others.append("--solexa-quals")
        }
        //报告参数
        case "-k"=>
        {
          this.others.append("-k")
          this.others.append(args(args.indexOf("-k")+1))
        }
        case "-a"=>
        {
          this.others.append("-a")
          this.others.append(args(args.indexOf("-a")+1))
        }
          //efford参数
        case "-D"=>
          {
            this.others.append("-D")
          }
        case other if(other.startsWith("-"))=>
          {
              println(other + " 该参数目前不支持！")

          }
        case _=>

          }
    }
  }
}
