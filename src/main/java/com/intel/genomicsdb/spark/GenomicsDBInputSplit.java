/*
 * The MIT License (MIT)
 * Copyright (c) 2016-2017 Intel Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.intel.genomicsdb.spark;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.log4j.Logger;
import org.apache.spark.Partition;
import java.util.ArrayList;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class GenomicsDBInputSplit extends InputSplit implements Writable {

  // Note that for now the assumption is that there is
  // one GenomicsDB instance per node, hence one host
  // per split
  String[] hosts;
  GenomicsDBPartitionInfo partition;
  ArrayList<GenomicsDBQueryInfo> queryRangeList;
  boolean isColumnPartitioned;
  long length = 0;

  Logger logger = Logger.getLogger(GenomicsDBInputSplit.class);

  /**
   * Default constructor
   */
  public GenomicsDBInputSplit() {
  }

  public GenomicsDBInputSplit(String loc) {
    hosts = new String[1];
    hosts[0] = loc;
  }

  public GenomicsDBInputSplit(GenomicsDBPartitionInfo partition, ArrayList<GenomicsDBQueryInfo> queryRangeList, boolean colPart) {
    this.partition = new GenomicsDBPartitionInfo(partition);
    this.queryRangeList = new ArrayList<GenomicsDBQueryInfo>(queryRangeList);
    this.isColumnPartitioned = colPart;
    // TODO: populate hosts if partition workspace is HDFS or S3?
    // this would help with locality
  }

  public GenomicsDBInputSplit(long length) {
    this.length = length;
  }

  public void write(DataOutput dataOutput) throws IOException {
    if (this.partition != null) {
      dataOutput.writeLong(this.partition.getBeginPosition());
      dataOutput.writeBoolean(this.isColumnPartitioned);
      Text.writeString(dataOutput, this.partition.getWorkspace());
      Text.writeString(dataOutput, this.partition.getArrayName());
      String vcfOutput = this.partition.getVcfOutputFileName();
      if (vcfOutput == null) {
        Text.writeString(dataOutput, "null");
      }
      else {
        Text.writeString(dataOutput, vcfOutput); 
      }
      if (queryRangeList == null) {
        dataOutput.writeInt(0);
      }
      else {
        dataOutput.writeInt(queryRangeList.size());
        for(GenomicsDBQueryInfo q: this.queryRangeList) {
          dataOutput.writeLong(q.getBeginPosition());
          dataOutput.writeLong(q.getEndPosition());
        }
      }
    }
    else {
      // write dummy value of -1 if we're using the posix fs
      // legacy inputsplits
      dataOutput.writeLong(-1);
    }
    dataOutput.writeLong(this.length);
  }

  public void readFields(DataInput dataInput) throws IOException {
    long _begin = dataInput.readLong();
    // if begin position is less than zero we don't leverage
    // partition info to create inputsplits (legacy posix fs case)
    if (_begin >= 0) {
      isColumnPartitioned = dataInput.readBoolean();
      String _workspace = Text.readString(dataInput);
      String _array = Text.readString(dataInput);
      String _vcfOutput = Text.readString(dataInput);
      if (_vcfOutput != null && _vcfOutput.equals("null"))
        _vcfOutput = null;
      partition = new GenomicsDBPartitionInfo(_begin, _workspace, _array, _vcfOutput);
      int qListLength = dataInput.readInt();
      if (qListLength > 0) {
        queryRangeList = new ArrayList<GenomicsDBQueryInfo>(qListLength);
        for(int i=0; i<qListLength; i++) {
          long begin = dataInput.readLong();
          long end = dataInput.readLong();
          queryRangeList.add(new GenomicsDBQueryInfo(begin, end));
        }
      }
      else {
        queryRangeList = null;
      }
    }
    length = dataInput.readLong();
  }

  public long getLength() throws IOException, InterruptedException {
    return this.length;
  }

  public GenomicsDBPartitionInfo getPartitionInfo() {
    return partition;
  }

  public ArrayList<GenomicsDBQueryInfo> getQueryInfoList() {
    return queryRangeList;
  }

  public boolean isColumnPartitioned() {
    return isColumnPartitioned;
  }

  /**
   * Called from {@link org.apache.spark.rdd.NewHadoopRDD#getPreferredLocations(Partition)}
   *
   * @return locations  The values of locations or nodes are passed from host file
   *                   in GenomicsDBConfiguration or hadoopConfiguration in SparkContext
   */
  public String[] getLocations() throws IOException, InterruptedException {
    if (hosts == null)
    {
      return new String[]{};
    }
    return hosts;
  }
}
