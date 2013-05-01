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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
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
  static int N = 679773;
  //compute filter parameters for netid ms2786
  static double fromNetID = 0.6872;
  static double rejectMin = 0.99 * fromNetID;
  static double rejectLimit = rejectMin + 0.01;
  static enum RecordCounters{ RESIDUAL_COUNTER };
  static int [] elements = {10328,20373,30629,40645,50462,60841,70591,80118,90497,100501,110567,120945,130999,140574,150953,161332,171154,181514,191625,202004,212383,222762,232593,242878,252938,263149,273210,283473,293255,303043,313370,323522,333883,343663,353645,363929,374236,384554,394929,404712,414617,424747,434707,444489,454285,464398,474196,484050,493968,503752,514131,524510,534709,545088,555467,565846,576225,586604,596585,606367,616148,626448,636240,646022,655804,665666,675448,685230};
  
  public static int blockIDofNode(int NodeID){
	  int startLook = NodeID / 10228;
	  while(startLook < elements.length){
		  if(startLook == 0) continue;
		  if(elements[startLook - 1] < NodeID && elements[startLook] > NodeID){
			  return startLook - 1;
		  }
	  }
	  return elements.length;
  }
  
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
    	//System.out.println("in Map");
    	String[] valueStrArray = value.toString().split(" ");
    	String u = key.toString();
    	String v = valueStrArray[1];
    	float prDivDeg = (float) (Float.parseFloat(valueStrArray[0]) * 1.0 / Float.parseFloat(valueStrArray[2]));
    	//spit out what we want

    	//set of vertices of its Block
    	output.collect(new Text(Integer.toString(blockIDofNode(Integer.parseInt(u)))), 
    			new Text(u + " " + v + " " + valueStrArray[0]+" "+ valueStrArray[2]));

    	//set of edges entering the block from the outside
    	output.collect(new Text(Integer.toString(blockIDofNode(Integer.parseInt(v)))),
    			new Text(v + " " + Float.toString(prDivDeg)));
    }
  }
  
  ///puts the offsets together as a String
  public static class Reduce extends MapReduceBase
    implements Reducer<Text, Text, Text, Text> {

    public void reduce(Text key, Iterator<Text> values,
                       OutputCollector<Text, Text> output,
                       Reporter reporter) throws IOException {
    	//ArrayList<String> toSend = new ArrayList<String>();
    	HashMap<String, ArrayList<Integer> > hmPRIn = new HashMap<String, ArrayList<Integer> >();
    	HashMap<String, ValueElt> hmInBlock = new HashMap<String, ValueElt>();
    	HashMap<String, ArrayList<Integer> > hmPRInIn = new HashMap<String, ArrayList<Integer> >();

    	int oldPR = 0;
	    while (values.hasNext()) {
	    	String x = values.next().toString();
	    	String[] eltArr = x.split(" ");
	    	if(eltArr.length == 2){
	    		if(hmPRIn.containsKey(eltArr[0])) {
	    			ArrayList<Integer> al = hmPRIn.get(eltArr[0]);
		    		al.add(Integer.parseInt(eltArr[1]));
	    		}
	    		else {
	    			ArrayList<Integer> al = new ArrayList<Integer>();
		    		hmPRIn.put(eltArr[0], al);
		    		al.add(Integer.parseInt(eltArr[1]));
		    	}
	    	}
	    	else {
	    		oldPR += Integer.parseInt(eltArr[2]);
	    		hmInBlock.put(eltArr[0], new ValueElt(eltArr[1], eltArr[2], eltArr[3]));
	    	}
	    	
    	}

<<<<<<< HEAD:mapreduce/PageRank/src/IndexWords.java
	    double newPR = (1 - d) * 1.0 / N + d * sum;
	    if(oldPR!=0){
	    	long residualLong = (long)(Math.abs(oldPR - newPR) * 1.0/newPR * 10000.0);
		    reporter.getCounter(RecordCounters.RESIDUAL_COUNTER).increment(residualLong);
	    }
	    for(String s:toSend){
	    	//if(deg==-1)
	    	//	System.out.println("EMIT-" + key.toString() + ":" + Double.toString(newPR) + " " + s + " " + Integer.toString(deg));
	    	output.collect(key, new Text(new String(Double.toString(newPR) + " " + s + " " + Integer.toString(deg))));
=======
    	boolean convergence = false;
    	
    	int finalVal = 0;
    	while(!convergence){
    		
    		int oldPRConvCal = 0;
    		for(String s : hmInBlock.keySet()){
    			oldPRConvCal += hmInBlock.get(s).PR;
    		}
    		
        	for(String s : hmInBlock.keySet()){
        		int insert = hmInBlock.get(s).PR;
        		String dest = hmInBlock.get(s).dest;
        		if(!hmPRInIn.containsKey(dest)){
        			hmPRInIn.put(dest, new ArrayList<Integer>());
        		}
        		ArrayList<Integer> insertArr = hmPRInIn.get(dest);
        		insertArr.add(insert);
        	}


    		for(String s : hmInBlock.keySet()){		
    			ValueElt ve = hmInBlock.get(s);
    			ArrayList<Integer> allIn = hmPRIn.get(s);
    			int sum = 0;
    			// boundary conditions
    			for(Integer i : allIn){
    				sum += i;
    			}
    			for(Integer i : hmPRInIn.get(s)){
    				sum += i;
    			}
    			sum = (int) (sum * d);
    			ve.PR = sum;
    		}
    		
    		int newPRConvCal = 0;
    		for(String s : hmInBlock.keySet()){
    			newPRConvCal += hmInBlock.get(s).PR;
    		}
    		finalVal = newPRConvCal - oldPRConvCal;
    		if(finalVal < 0) finalVal *= -1;
    		finalVal = finalVal / newPRConvCal / hmInBlock.size();
    		
    		if(finalVal < 0.001){
    			convergence = true;
    		}
    	}

	    // double newPR = (1 - d) * 1.0 / N + d * sum;
	    // long residualLong = (long)(Math.abs(oldPR - newPR) * 1.0/newPR * 10000.0);
    	long residualLong = (long) (finalVal * 10000.0);
	    reporter.getCounter(RecordCounters.RESIDUAL_COUNTER).increment(residualLong);
	    for(String s:hmInBlock.keySet()){
	    	output.collect(new Text(s), 
	    			new Text(hmInBlock.get(s).PR + " " + hmInBlock.get(s).dest + " " + hmInBlock.get(s).deg));
>>>>>>> 97b1cf11f5fd46a612aaa7fbdb9561c90d794887:mapreduceBlock/src/IndexWords.java
	    }
    }
  }

  public int run(String[] args) throws Exception {
	  if(args.length < 2){
		  return -1;
	  }
  	
	  checkWords = new String[args.length-2];
	  
	  int numIter = 5;
	  
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
		  System.out.println(N+" "+(resVal/(1.0*N)));
		  if(resVal/(1.0*N) < 0.001) break;
	  }
	
	  return 0;
  }

  public static void filterFile(String input, String writeOut){
	  try {
        BufferedReader in = new BufferedReader(new FileReader(input));
        FileWriter fstream = new FileWriter(writeOut);
        BufferedWriter out = new BufferedWriter(fstream);
		String line;
		while((line =in.readLine()) != null) {
			if(selectInputLine(Double.parseDouble("0."+line.split("\\.")[1]))){
				out.write(line+"\n");
			}
			else {
				System.out.println(line);
			}
		}
		out.close();
	  } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
  }
  public static void filterFile(String input, String writeOut, Double min, Double max){
	  try {
		  BufferedReader in = new BufferedReader(new FileReader(input));
		  FileWriter fstream = new FileWriter(writeOut);
		  BufferedWriter out = new BufferedWriter(fstream);
		  String line;
		  while((line =in.readLine()) != null) {
			if(min<Double.parseDouble("0."+line.split("\\.")[1]) && Double.parseDouble("0."+line.split("\\.")[1])<max){
				out.write(line+"\n");
			}
		}
		out.close();
	  } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
  }
  public static void formatFile(String input, String writeOut){
	  try {
		N=0;
		String curNode="-1";
		HashMap<String,Integer> degMap= new HashMap<String,Integer>();
		BufferedReader in = new BufferedReader(new FileReader(input));
        FileWriter fstream = new FileWriter(writeOut);
        BufferedWriter out = new BufferedWriter(fstream);
		String line;
		while((line =in.readLine()) != null) {
			String [] split = line.trim().split("\\s+");
			if(!split[0].equals(curNode)){
				//System.out.println(split[0]);;
				N++;
				degMap.put(split[0], 1);
				curNode=split[0];
			}
			else {
				degMap.put(split[0], degMap.get(split[0])+1);
			}
		}
		System.out.println(N);
		double invN = 1.0/N;
		in.close();
		in=new BufferedReader(new FileReader(input));
		while((line =in.readLine()) != null) {
			String [] split = line.trim().split("\\s+");
			if(split[1].contains(".")){
				System.out.println(line);
				return;
			}
			out.write(split[0]+"\t"+invN+" "+split[1]+" "+degMap.get(split[0])+"\n");
		}
		out.close();
	  } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
  }
  public static void cleanFiles(String folder, String dirName){
	  String[] files =new File(folder).list();
	  for(String filename : files){
		  if(filename.contains(dirName)){
			  try {
				delete(new File(filename));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
	  }
  }
  static void delete(File f) throws IOException {
	  if (f.isDirectory()) {
	    for (File c : f.listFiles())
	      delete(c);
	  }
	  if (!f.delete())
	    throw new FileNotFoundException("Failed to delete file: " + f);
	}
  public static void main(String[] args) throws Exception {
	  
	  //filterFile("edges.txt","ms2786edges.txt");
	  //filterFile("ms2786edges.txt","usethese.txt",0.0,.2);
	  //formatFile("ms2786edges.txt","ourFormat.txt");
	  cleanFiles("./",args[1]);
	    int res = ToolRunner.run(new Configuration(), new IndexWords(), args);
      System.exit(res);
  }

}