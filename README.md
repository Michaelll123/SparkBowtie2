# Brief introduction of SparkBowtie2 #

**SparkBowtie2** is a new distributed sequence alignment software. It combines the [Apache Spark][1] with the sequence alignment software —— [Bowtie2][2], takes advantage of Spark in high processing efficiency and has a better performance than Bowtie2 in terms of running time. As is known that it is not likely for Java to call C++ codes directly and Bowtie2 is written in C++ language and Spark is running on Java Virtual Machine (JVM).Thus, the [JNI][3] (Java Native Interface) technology is used to establish the calls between different programming languages, which avoids the trouble of installing Bowtie2 on each worker node in the Spark cluster.  

SparkBowtie2 has the following characteristics:

 **i)** The sequence alignment result is exactly identical with the serial result of Bowtie2.

 **ii)** It contains the original options and parameters of Bowtie2, and users do not need to change the usage habits.

 **iii)** The running time of SparkBowtie2 is affected by the number of Worker Nodes, RDD partitions, and the multithreading. the parameter is recommended in the corresponding paper to obtain the optimal performance of sequence alignments. The experimental results show that SparkBowtie2 is about 6-10 times faster than Bowtie2 in the sequence alignments of genome or transcriptome and it is obvious that the efficient and reliable performance of SparkBowtie2 is of value to researches and applications.

# Structure #
The project keeps a standard Maven structure. The source code is in the *src/main* folder. Inside it, we can find two subfolders:

* **java** - Here is where the Java code is stored.
* **resource** - the dynamic link library file (libsparkbowtie2.so) which is the result of [JNI][3] technology is stored here.
* **scala** - Here is where the scala code is stored and main function (spark_bowtie2.scala) is here .

# Getting started #

## Requirements

* **Spark Cluster** - Make sure that Apache Hadoop and Spark has been installed and configured properly.
* **genomic sequence files** - Including the reference genome file and the genomic reads you hope to analyse.
* **Index files** - To create an index for the reference genome ,you have to create a new temporary directory (it doesn’t matter where), change into that directory, and run:

	$BT2_HOME/bowtie2-build /data/Index index

## Building

The default way to build **SparkBowtie2** is:

	git clone https://github.com/Michaelll123/SparkBowtie2.git
	cd SparkBowtie2
	mvn package
you can also directly use the SparkBowtie2-1.0-SNAPSHOT.jar.

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
* **-S** - The path of output. 
The last three arguments are all HDFS files or paths.
More options can be found [here][5] 


After the execution, you can find the result file (***.sam) in the /OutputFile on HDFS, and it is a complete file merged from the output of every worker node.

# How to cite SparkBowtie2? #
**Please cite the paper below:**

Yu Jiantao, Bian Enze, Liu Shengdong, Wang Taiming, Hui Yixiang, Guo Maozu. Methods of designing and implementing the software of SparkBowtie2 for parallelized sequence alignments. (submittd to the Journal of Harbin Institute of Technology(in Chinese))

[1]: https://spark.apache.org/
[2]: http://bowtie-bio.sourceforge.net/bowtie2/index.shtml
[3]: http://www.cs.technion.ac.il/~gabr/papers/cuj_jni.pdf
[4]: http://www.1000genomes.org/
[5]: http://bowtie-bio.sourceforge.net/bowtie2/manual.shtml#options
