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
  static double d = 0.86;
  static int N = 679773;
  //static int N = 7;
  //compute filter parameters for netid ms2786
  static double fromNetID = 0.6872;
  static double rejectMin = 0.99 * fromNetID;
  static double rejectLimit = rejectMin + 0.01;
  static enum RecordCounters{ RESIDUAL_COUNTER , LOOP_COUNTER};
  static int [] elements = {10328,20373,30629,40645,50462,60841,70591,80118,90497,100501,110567,120945,130999,140574,150953,161332,171154,181514,191625,202004,212383,222762,232593,242878,252938,263149,273210,283473,293255,303043,313370,323522,333883,343663,353645,363929,374236,384554,394929,404712,414617,424747,434707,444489,454285,464398,474196,484050,493968,503752,514131,524510,534709,545088,555467,565846,576225,586604,596585,606367,616148,626448,636240,646022,655804,665666,675448,685230};
  //static int [] elements = {2,4,6,8};
  static int numDiv = 10228;
  //static int numDiv = 3;
  
  public static int blockIDofNode(int NodeID){
	  int startLook = NodeID / numDiv;
	  while(startLook < elements.length - 1){
		  if(elements[0] > NodeID) return 0;
		  if(startLook == 0){
			  startLook += 1;
			  continue;
		  }
		  else if(elements[startLook - 1] > NodeID && elements[startLook] > NodeID){
			  return startLook;
		  }
		  startLook += 1;
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

    	//System.out.println(blockIDofNode(Integer.parseInt(u)) + ": " + u + "->" + v + " " + valueStrArray[0]+" "+ valueStrArray[2]);
    	//set of vertices of its Block
    	//if(blockIDofNode(Integer.parseInt(u)) == 0) System.out.println("IN-" + blockIDofNode(Integer.parseInt(u)) + ": " + u + " " + v + " " + valueStrArray[0]+" "+ valueStrArray[2]);
    	output.collect(new Text(Integer.toString(blockIDofNode(Integer.parseInt(u)))), 
    			new Text(u + " " + v + " " + valueStrArray[0]+" "+ valueStrArray[2]));

    	//set of edges entering the block from the outside
    	if(blockIDofNode(Integer.parseInt(v)) != blockIDofNode(Integer.parseInt(u))){
        	//if(blockIDofNode(Integer.parseInt(v)) == 0) System.out.println("OU-" + blockIDofNode(Integer.parseInt(v)) + ": " + v + " " + Float.toString(prDivDeg));
    		output.collect(new Text(Integer.toString(blockIDofNode(Integer.parseInt(v)))),
	    			new Text(v + " " + Float.toString(prDivDeg)));
    	}
    }
  }
  
  ///puts the offsets together as a String
  public static class Reduce extends MapReduceBase
    implements Reducer<Text, Text, Text, Text> {

    public void reduce(Text key, Iterator<Text> values,
                       OutputCollector<Text, Text> output,
                       Reporter reporter) throws IOException {

    	HashMap<String, Double> oriBlockPR = new HashMap<String, Double> ();
    	HashMap<String, Double> inBlockPR = new HashMap<String, Double> ();
    	HashMap<String, ArrayList<ValueElt> > allData = new HashMap<String, ArrayList<ValueElt> > ();
    	HashMap<String, Double> boundaryToNode = new HashMap<String, Double> ();
    	ArrayList<ValueElt> otherInformation = new ArrayList<ValueElt> ();
    	Set<String> inBlock = new HashSet<String>();
    	
	    while (values.hasNext()) {
	    	String x = values.next().toString();
	    	String[] eltArr = x.split(" ");
	    	if(eltArr.length == 2){
	    		if(boundaryToNode.containsKey(eltArr[0])) {
	    			Double d = boundaryToNode.get(eltArr[0]);
	    			Double sum = d + Double.parseDouble(eltArr[1]);
	    			boundaryToNode.put(eltArr[0], new Double(sum));

	    		}
	    		else {
	    			boundaryToNode.put(eltArr[0], new Double(Double.parseDouble(eltArr[1])));
		    	}
	    	}
	    	else {
	    		if(!allData.containsKey(eltArr[1])){
	    			allData.put(eltArr[1], new ArrayList<ValueElt>());
	    		}
	    		ArrayList<ValueElt> arrList = allData.get(eltArr[1]);
	    		arrList.add(new ValueElt(eltArr[0], eltArr[2], eltArr[3]));
	    		
	    		inBlockPR.put(eltArr[0], Double.parseDouble(eltArr[2]));
	    		oriBlockPR.put(eltArr[0], Double.parseDouble(eltArr[2]));
	    		otherInformation.add(new ValueElt(eltArr[0], eltArr[1], eltArr[2], eltArr[3]));
	    		inBlock.add(eltArr[0]);
	    	}
    	}
	    
	    boolean convergence = false;
	    
	    long numLoops = 0;
	    while(convergence == false){
	    	numLoops += 1;
	    	double oldPRSum = 0;
	    	double newPRSum = 0;
	    	double diff = 0;
	    	for(String k : inBlockPR.keySet()){
	    		oldPRSum += inBlockPR.get(k);
	    	}
	    	
		    for(String k : inBlock){
		    	double PRSum = 0;
		    	if(allData.containsKey(k)){
			    	for(ValueElt edge : allData.get(k)){
			    		PRSum += edge.PR * 1.0 / edge.deg;
				    	//System.out.println("Calc for: " + edge.PR + " is: " + edge.deg);
			    	}
		    	}
		    	//System.out.println("PR SUM for: " + k + " is: " + PRSum);
		    	//System.out.println("Actually for: " + k + " is: " + ((1 - d)*1.0/N + d * 1.0 * PRSum));
		    	if(boundaryToNode.containsKey(k)) PRSum += boundaryToNode.get(k);
		    	double oldSpecPR = 0;
		    	oldSpecPR = inBlockPR.get(k);
		    	inBlockPR.put(k, new Double((1 - d)*1.0/N + d * 1.0 * PRSum));
		    	newPRSum += (1 - d)*1.0/N + d * 1.0 * PRSum;
		    	diff += Math.abs((1 - d)*1.0/N + d * 1.0 * PRSum - oldSpecPR);
		    }
		    
		    for(String k : allData.keySet()){
		    	for(ValueElt edge : allData.get(k)){
		    		edge.PR = inBlockPR.get(edge.source);
		    	}
		    }
		    
		    double residual = (Math.abs(diff) * 1.0/newPRSum)/inBlock.size();
		    //System.out.println(residual + " "+ diff+ " " + oldPRSum + " " + newPRSum);
		    if(residual < 0.00000001){
		    	convergence = true;
		    }
	    }
	    
	    long residual = 0;
	    for(String k : oriBlockPR.keySet()){
	    	residual += (long)(10000*(Math.abs(oriBlockPR.get(k) - inBlockPR.get(k))/inBlockPR.get(k)));
	    	//System.out.println("Ori: " + oriBlockPR.get(k) + "in: " + inBlockPR.get(k));
	    }
	    //System.out.println(residual);
    	long residualLong = (long) (residual);
	    // double newPR = (1 - d) * 1.0 / N + d * sum;
	    // long residualLong = (long)(Math.abs(oldPR - newPR) * 1.0/newPR * 10000.0);
    	//long residualLong = (long) (finalVal * 10000.0);
	    reporter.getCounter(RecordCounters.RESIDUAL_COUNTER).increment(residualLong);
	    reporter.getCounter(RecordCounters.LOOP_COUNTER).increment(numLoops);
	    for(ValueElt ve:otherInformation){
	    	output.collect(new Text(ve.source), 
	    			new Text(inBlockPR.get(ve.source) + " " + ve.dest + " " + ve.deg));
	    }
    }
  }
  
  public double trimNum(double val){
	  double retVal = val * 10000;
	  long retValLong = (long) retVal;
	  return (double)(retValLong/10000);
  }
  
  public double getResidual(int iter, String filename){
	  String filename1 = filename;
	  String filename2 = "output0/part-00000";
	  HashMap<String, Double> hm = new HashMap<String, Double>();
	  HashMap<String, Integer> hm2 = new HashMap<String, Integer>();
	  double sumDiff = 0;
	  double sumBot = 0;
	  if(iter > 0){
		  filename1 = "output" + Integer.toString(iter - 1) +"/part-00000";
		  filename2 = "output" + Integer.toString(iter) + "/part-00000";
	  }
	  try {
	        BufferedReader in = new BufferedReader(new FileReader(filename1));
			String line;
			while((line =in.readLine()) != null) {
				String[] lineParams = line.split("\\s+");
				if(hm.containsKey(lineParams[0])) continue;
				else{
					hm.put(lineParams[0], Double.parseDouble(lineParams[1]));
				}
			}
			in.close();
	        in = new BufferedReader(new FileReader(filename2));
			while((line =in.readLine()) != null) {
				String[] lineParams = line.split("\\s+");
				if(hm2.containsKey(lineParams[0])) continue;
				else{
					sumDiff += trimNum(Math.abs(Double.parseDouble(lineParams[1]) - hm.get(lineParams[0]))/Double.parseDouble(lineParams[1]));
					sumBot += Double.parseDouble(lineParams[1]);
					hm2.put(lineParams[0], 1);
					//System.out.println("Diff: " + (Math.abs(Double.parseDouble(lineParams[1]) - hm.get(lineParams[0]))/Double.parseDouble(lineParams[1])));
				}
			}
			in.close();
			return sumDiff * 1.0;
		  } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
	  
	  return 0;
  }

  public int run(String[] args) throws Exception {
	  if(args.length < 2){
		  return -1;
	  }
  	
	  checkWords = new String[args.length-2];
	  
	  int numIter = 10;
	  
	  Path input = new Path(args[0]);
	  
	  for(int i = 0; i < numIter; i++){
		  System.out.println("Iter: " + i);
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
		  double loopVal = rj.getCounters().getCounter(RecordCounters.LOOP_COUNTER) * 1.0 / elements.length;
		  System.out.println("AVg Num loops: " + loopVal);
		  //double resVal = getResidual(i,args[0]);
		  System.out.println(N+" "+(resVal/(1.0*N)));
		  if(resVal/(1.0 * N) < 0.001){
			  System.out.println(resVal);
			  System.out.println(resVal/(1.0 * N));
			  System.out.println((resVal/(1.0 * N) < 0.001));
			  break;
		  }
	  }
	
	  return 0;
  }

  public static void filterFile(String input, String writeOut){
	  try {
        BufferedReader in = new BufferedReader(new FileReader(input));
        FileWriter fstream = new FileWriter(writeOut);
        BufferedWriter out = new BufferedWriter(fstream);
		String line;
		int numAll = 0;
		int numEx = 0;
		while((line =in.readLine()) != null) {
			//System.out.println(line.split("\\s+")[3]);
			numAll += 1;
			if(line.split("\\s+").length > 3 && selectInputLine(Double.parseDouble(line.split("\\s+")[3]))){
				out.write(line+"\n");
			}
			else if(line.split("\\s+").length <= 3){
				out.write(line+"\n");
			}
			else {
				numEx += 1;
				if(line.split("\\s+").length > 3 ){
				//System.out.println(line.split("\\s+")[3]);
				//System.out.println(selectInputLine(Double.parseDouble(line.split("\\s+")[3])));
				}
			}
		}
		System.out.println(numEx + " / " + numAll);
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
			else{
				//System.out.println(Double.parseDouble("0."+line.split("\\.")[1]));
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
	  //filterFile("ms2786edges.txt","usethese.txt",0.0,1.0);
	  //formatFile("usethese.txt","ourFormat.txt");
	  System.out.println("N: " + N);
	  cleanFiles("./",args[1]);
	  int res = ToolRunner.run(new Configuration(), new IndexWords(), args);
      System.exit(res);
  }

}