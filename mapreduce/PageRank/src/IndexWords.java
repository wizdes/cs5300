/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.KeyValueTextInputFormat;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class IndexWords extends Configured implements Tool {

  static String[] checkWords;
  static double d = 0.85;
  static int N = 10;
  //compute filter parameters for netid ms2786
  static double fromNetID = 0.6872;
  static double rejectMin = 0.99 * fromNetID;
  static double rejectLimit = rejectMin + 0.01;
  static enum RecordCounters{ RESIDUAL_COUNTER };
  //assume 0.0 <= rejectMin < rejectLimit <= 1.0
  public static boolean selectInputLine(double x) {
		return ( ((x >= rejectMin) && (x < rejectLimit)) ? false : true );
  }
  ///checks to see if a token had a word that exists in checkWords
  ///if so it adds that word as well as its offset
  public static class MapClass extends MapReduceBase
    implements Mapper<Text, Text, Text, Text> {

    public void map(Text key, Text value,
                    OutputCollector<Text, Text> output,
                    Reporter reporter) throws IOException {
    	
    	System.out.println("in Map");
    	String[] valueStrArray = value.toString().split(" ");
    	String u = key.toString();
    	String v = valueStrArray[1];
    	float deg = (float) (Float.parseFloat(valueStrArray[0]) * 1.0 / Float.parseFloat(valueStrArray[2]));
    	//spit out what we want
    	output.collect(new Text(u), new Text(v + " 0 " + valueStrArray[0]));
    	output.collect(new Text(v), new Text(Float.toString(deg) + " " + valueStrArray[2]));
    }
  }
  
  ///puts the offsets together as a String
  public static class Reduce extends MapReduceBase
    implements Reducer<Text, Text, Text, Text> {

    public void reduce(Text key, Iterator<Text> values,
                       OutputCollector<Text, Text> output,
                       Reporter reporter) throws IOException {
    	System.out.println("In Reduce");
    	double sum = 0;
    	ArrayList<String> toSend = new ArrayList<String>();
    	
    	int deg = -1;
    	System.out.println(key.toString() + "->");
    	double oldPR = 0;
	    while (values.hasNext()) {
	    	String x = values.next().toString();
	    	String[] eltArr = x.split(" ");
	    	System.out.println(x);
	    	if(eltArr.length == 3){
	    		//System.out.println("just one!");
	    		toSend.add(eltArr[0]);
	    		oldPR = Double.parseDouble(eltArr[2]);
	    	}
	    	else {
	    		sum = sum + Double.parseDouble(eltArr[0]);
	    		deg = Integer.parseInt(eltArr[1]);
	    	}
    	}
	    double newPR = (1 - d) * 1.0 / N + d * sum;
	    long residualLong = (long)(Math.abs(oldPR - newPR) * 1.0/newPR * 10000.0);
	    System.out.println(reporter.getCounter(RecordCounters.RESIDUAL_COUNTER)+" + res long"+residualLong);
	    reporter.getCounter(RecordCounters.RESIDUAL_COUNTER).increment(residualLong);
	    for(String s:toSend){
	    	System.out.println("EMIT-" + key.toString() + ":" + Double.toString(newPR) + " " + s + " " + Integer.toString(deg));
	    	output.collect(key, new Text(new String(Double.toString(newPR) + " " + s + " " + Integer.toString(deg))));
	    }
    }
  }

  public int run(String[] args) throws Exception {
	  if(args.length < 2){
		  return -1;
	  }
  	
	  checkWords = new String[args.length-2];
	  
	  int numIter = 20;
	  
	  Path input = new Path(args[0]);
	  
	  for(int i = 0; i < numIter; i++){
		  JobConf conf = new JobConf(getConf(), IndexWords.class);
		  conf.setJobName("indexwords");
		
		  conf.setInputFormat(KeyValueTextInputFormat.class);
		  conf.setOutputFormat(TextOutputFormat.class);

		  conf.setOutputKeyClass(Text.class);
		  conf.setOutputValueClass(Text.class);
		
		  conf.setMapperClass(MapClass.class);
		  conf.setReducerClass(Reduce.class);
		
		  FileInputFormat.setInputPaths(conf, input);
		  FileOutputFormat.setOutputPath(conf, new Path(args[1] + Integer.toString(i)));

		  RunningJob rj = JobClient.runJob(conf);
		  input = new Path(args[1]+ Integer.toString(i));
		  double resVal = rj.getCounters().getCounter(RecordCounters.RESIDUAL_COUNTER) * 1.0/10000;
		  System.out.println(resVal);
		  if(resVal < 0.001) break;
		  else{
			  rj.getCounters().incrCounter(RecordCounters.RESIDUAL_COUNTER, (long) (-1 * resVal * 10000));
		  }
	  }
	
	  return 0;
  }

  public static void filterFile(String input, String writeOut){
	  try {
		List<String> lines=Files.readAllLines(FileSystems.getDefault().getPath(input), Charset.forName("UTF-8"));
		BufferedWriter writer = Files.newBufferedWriter(FileSystems.getDefault().getPath(writeOut), Charset.forName("UTF-8"));
		for(String line : lines){
			if(selectInputLine(Double.parseDouble("0."+line.split("\\.")[1]))){
				writer.write(line);
			}
			else {
				System.out.println(line);
			}
		}
		writer.close();
	  } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
  }
  public static void main(String[] args) throws Exception {
	  filterFile("edges.txt","ms2786edges.txt");
	  //  int res = ToolRunner.run(new Configuration(), new IndexWords(), args);
   // System.exit(res);
  }

}