Brief introduction of SparkBowtie2 
SparkBowtie2 is a new distributed sequence alignment software which combines the Spark technology with the sequence alignment software, Bowtie2, takes advantage of Spark in high processing efficiency, and promotes the Bowtie2 performance. In the process of the development, because Bowtie2 is written by C++ language, Spark is running on Java Virtual Machine (JVM), and it is impossible from Java to call C++ codes directly, the JNI (Java Native Interface) technology is used to establish the calls between different programming languages, avoiding the trouble of installing Bowtie2 on each Worker Node in the Spark cluster, with the flexibility of parallel invocations.
SparkBowtie2 has the following characteristics:
 i) The sequence alignment result is exactly identical with the serial result of Bowtie2.
 ii) It contains the original options and parameters of Bowtie2, and users do not need to change the usage habits.
 iii) The running time of SparkBowtie2 is affected by the number of Worker Nodes, RDD partitions, and the multithreading. The experimental results show that SparkBowtie2 is about 7-10 times faster than Bowtie2 in the sequence alignments of genome or transcriptome, with the efficient and reliable performances, and is of a value of research and applications.All of them work with paired and single-end reads.
Structure
The project keeps a standard Maven structure. The source code is in the src/main folder. Inside it, we can find three subfolders:
•	java - Here is where the Java code is stored.
•	resource – the dynamic link library file (libsparkbowtie2.so) which is is stored here.
•	scala - Here is where the scala code is stored and main function (spark_bowtie2.scala) is here . 
Getting started
Requirements
Requirements to build BigBWA are the same than the ones to build BWA, with the only exception that the JAVA_HOMEenvironment variable should be defined. If not, you can define it in the /src/main/native/Makefile.common file.
It is also needed to include the flag -fPIC in the Makefile of the considered BWA version. To do this, the user just need to add this option to the end of the CFLAGS variable in the BWA Makefile. Considering bwa-0.7.15, the original Makefile contains:
CFLAGS=		-g -Wall -Wno-unused-function -O2
and after the change it should be:
CFLAGS=		-g -Wall -Wno-unused-function -O2 -fPIC
Additionaly, and as BigBWA is built with Maven since version 0.2, also have it in the user computer is needed.
Building
The default way to build BigBWA is:
git clone https://github.com/citiususc/BigBWA.git
cd BigBWA
mvn package
This will create the target folder, which will contain the jar file needed to run BigBWA:
•	BigBWA-2.1.jar - jar file to launch with Hadoop.
Configuring
Since version 2.0 there is no need of configuring any Hadoop parameter. The only requirement is that the YARN containers need to have at least 7500MB of memory available (for the human genome case).
Running BigBWA
BigBWA requires a working Hadoop cluster. Users should take into account that at least 7500MB of free memory per map are required (each map loads into memory the bwa index). Note that BigBWA uses disk space in the Hadoop tmp directory.
Here it is an example of how to run BigBWA using the BWA-MEM paired algorithm. This example assumes that our index is stored in all the cluster nodes at /Data/HumanBase/ . The index can be obtained with BWA, using "bwa index".
First, we get the input Fastq reads from the 1000 Genomes Project ftp:
wget ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/data/NA12750/sequence_read/ERR000589_1.filt.fastq.gz
wget ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/data/NA12750/sequence_read/ERR000589_2.filt.fastq.gz
Next, the downloaded files should be uncompressed:
gzip -d ERR000589_1.filt.fastq.gz
gzip -d ERR000589_2.filt.fastq.gz
and prepared to be used by BigBWA:
python src/utils/Fq2FqBigDataPaired.py ERR000589_1.filt.fastq ERR000589_2.filt.fastq ERR000589.fqBD

hdfs dfs -copyFromLocal ERR000589.fqBDP ERR000589.fqBDP
Finally, we can execute BigBWA on the Hadoop cluster:
yarn jar BigBWA-2.1.jar com.github.bigbwa.BigBWA -D mapreduce.input.fileinputformat.split.minsize=123641127
-D mapreduce.input.fileinputformat.split.maxsize=123641127
-D mapreduce.map.memory.mb=7500
-w "-R @RG\tID:foo\tLB:bar\tPL:illumina\tPU:illumina\tSM:ERR000589 -t 2"
-m -p --index /Data/HumanBase/hg19 -r ERR000589.fqBDP ExitERR000589
Options:
•	-m - Sequence alignment algorithm.
•	-p - Use paired-end reads.
•	-w "args" - Can be used to pass arguments directly to BWA (ex. "-t 4" to specify the amount of threads to use per instance of BWA).
•	--index index_prefix - Index prefix is specified. The index must be available in all the cluster nodes at the same location.
•	The last two arguments are the input and output HDFS files.
If you want to check all the available options, execute the command:
yarn jar BigBWA-2.1.jar com.github.bigbwa.BigBWA -h
The commands are:
BigBWA performs genomic alignment using bwa in a Hadoop/YARN cluster
 usage: yarn jar --class com.github.bigbwa.BigBWA BigBWA-2.1.jar
       [-a | -b | -m] [-h] [-i <Index prefix>]   [-n <Number of
       partitions>] [-p | -s] [-r]  [-w <"BWA arguments">]
       <FASTQ file> <SAM file output>
Help options: 
  -h, --help                                       Shows this help

Input FASTQ reads options: 
  -p, --paired                                     Paired reads will be used as input FASTQ reads
  -s, --single                                     Single reads will be used as input FASTQ reads

BWA algorithm options: 
  -a, --aln                                        The ALN algorithm will be used
  -b, --bwasw                                      The bwasw algorithm will be used
  -m, --mem                                        The MEM algorithm will be used

Index options: 
  -i, --index <Index prefix>                       Prefix for the index created by bwa to use - setIndexPath(string)

Spark options: 
  -n, --partitions <Number of partitions>          Number of partitions to divide input - setPartitionNumber(int)

Reducer options: 
  -r, --reducer                                    The program is going to merge all the final results in a reducer phase

BWA arguments options: 
  -w, --bwa <"BWA arguments">                      Arguments passed directly to BWA
After the execution, to move the output to the local filesystem use:
hdfs dfs -copyToLocal ExitERR000589/part-r-00000 ./
In case there is no reducer, the output will be split into several pieces. In order to put it together users could use one of our Python utils or "samtools merge":
hdfs dfs -copyToLocal ExitERR000589/Output* ./
python src/utils/FullSam.py ./ ./OutputFile.sam
##Frequently asked questions (FAQs)
1.	I can not build the tool because jni_md.h or jni.h is missing.
####1. I can not build the tool because jni_md.h or jni.h is missing. You need to set correctly your JAVA_HOME environment variable or you can set it in Makefile.common.



# Brief introduction of SparkBowtie2 #

**SparkBowtie2** is a new distributed sequence alignment software which combines the .[Apache Spark].[1] with the sequence alignment software, .[Bowtie2].[2], takes advantage of Spark in high processing efficiency, and promotes the Bowtie2 performance. In the process of the development, because Bowtie2 is written by **C++** language, Spark is running on Java Virtual Machine (JVM), and it is impossible from Java to call C++ codes directly, the .[JNI].[3] (Java Native Interface) technology is used to establish the calls between different programming languages, avoiding the trouble of installing Bowtie2 on each Worker Node in the Spark cluster, with the flexibility of parallel invocations.

SparkBowtie2 has the following characteristics:
 **i)** The sequence alignment result is exactly identical with the serial result of Bowtie2.
 **ii)** It contains the original options and parameters of Bowtie2, and users do not need to change the usage habits.
 **iii)** The running time of SparkBowtie2 is affected by the number of Worker Nodes, RDD partitions, and the multithreading. The experimental results show that SparkBowtie2 is about 7-10 times faster than Bowtie2 in the sequence alignments of genome or transcriptome, with the efficient and reliable performances, and is of a value of research and applications.

# Structure #
The project keeps a standard Maven structure. The source code is in the *src/main* folder. Inside it, we can find two subfolders:

* **java** - Here is where the Java code is stored.
* **resource** - the dynamic link library file (libsparkbowtie2.so) which is the result of .[JNI].[3] technology is stored here.
* **scala** - Here is where the scala code is stored and main function (spark_bowtie2.scala) is here .

# Getting started #

## Requirements

* **Spark Cluster** - Make sure that Apache Hadoop and Spark has been installed and configured properly.
* **genomic sequence files** - Including the reference genome file and the genomic reads you hope to analyse.
* **Index files** - To create an index for the reference genome ,you have to create a new temporary directory (it doesn’t matter where), change into that directory, and run:

$BT2_HOME/bowtie2-build /data/Index index
FASTQ_FILE_PATH is the path where your fastq file locates.

## Building
Given that the Maven project file has been provided, you can just download the file and import it into IDEA to build. 
The executable jar file has also been provided if you don’t want to build it by yourself.

## Running SparkBowtie2 ##
**SparkBowtie2** requires a working Spark cluster. Users should take into account that at least 7500MB of free memory per worker node are required.

Here it is an example of how to run **SparkBowtie2** .This example assumes that our index is stored in the master nodes at */data/Index/*.The method to get these index files has been mentioned above. 

First, we get the input Fastq reads from the [1000 Genomes Project][4] ftp:
**(if you already have any other reads on your cluster node then you can skip this step.)**
	wget –O /data ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/data/NA12750/sequence_read/ERR000589_1.filt.fastq.gz
		wget –O /data ftp://ftp.1000genomes.ebi.ac.uk/vol1/ftp/data/NA12750/sequence_read/ERR000589_2.filt.fastq.gz
	
Next, the downloaded files should be uncompressed:

	gzip -d ERR000589_1.filt.fastq.gz
	gzip -d ERR000589_2.filt.fastq.gz
	
Then,we have to upload these two fastq reads to HDFS: 
	$HADOOP_HOME/bin/hdfs dfs -put /data/ERR000589_1.filt.fastq /data
	$HADOOP_HOME/bin/hdfs dfs -put /data/ERR000589_2.filt.fastq /data
Finally, we can execute **SparkBowtie2** on the Spark cluster:

	$SPARK_HOME/bin/spark-submit –master username://hostname:7077 –driver-memory 1500m –executor-cores 1 –class spark_bowtie2 ~/SparkBowtie2-1.0-SNAPSHOT.jar –x /data/Index/index  -1 /data/ERR000589_1.filt.fastq  -2 /data/ERR000589_2.filt.fastq  -S /OutputFile 

Options:

* **-x** - Index of reference genome
* **-1** - First genomic reads .
* **-2** - Second genomic reads.
* **--S** - The path of output. 
The last three arguments are all HDFS files or paths.
More options can be found .[here].[5] 


After the execution, you can find the result file (***.sam) in the /OutputFile on HDFS, and it is a complete file merged from the output of every worker node.

.[1]: https://spark.apache.org/
.[2]: http://bowtie-bio.sourceforge.net/bowtie2/index.shtml
.[3]: http://www.cs.technion.ac.il/~gabr/papers/cuj_jni.pdf
.[4]: http://www.1000genomes.org/
.[5]: http://bowtie-bio.sourceforge.net/bowtie2/manual.shtml#options
