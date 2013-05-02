
For our Simple Page Rank algorithm:
Map:
Input:      < u, (PRt(u), v, deg(u)) >
Output: < u, (v, deg(u) | u -> v ) >
Source, gives structure and deg(u)
Output: < v, (PRt(u)/deg(u)) >
Destination, 

Reduce:
Output: < u, (PRt+1(u), v, deg(u)) >

For our Block Page Rank algorithm:
Map:
Input:    < u, (PRt(u), v, deg(u)) >
Output: < blockID(u), (u, v, PR(u), deg(u)) >
Non-boundary(u,v inside block)
Output: < blockID(v), (v, PR(u)/deg(u)) >
Boundary

Reduce:
Output: < u, (PRt+1(u), v, deg(u)) >

We also implemented the extra credit: Random blocks.

Our data structures:
HashMap<String, Double> oriBlockPR;
- <Node, PR>
- Original block PR
HashMap<String, Double> inBlockPR;	
- <Node, PR>
- PR being updated inside block
HashMap<String, Double> boundaryToNode;
- <Node, PR>
- PR of boundary nodes
HashMap<String, ArrayList<ValueElt>> allData;
- <Node, List of data passed in>
- Helper data structure for algorithm


We used ms2786 as our netid, thus rejectMin=0.680328 and rejectLimit=0.690328
Our solution takes in the edge.txt file, which should be copied into the directory, generates a file that corresponds to our userID, and then converts it into our format with some precomupted results.
Our format is as follows
NodeIn	PageRank NodeOut Degree(NodeIn)

Since Degree(NodeIn) never changes we compute it once and store it.  This goes out into the file with the name specified in argument 1.

To run our code run mapReduce.jar ourFormat.txt outputDir
Where ourFormat.txt is the name of the file to put our formated file into
And outputDir is the prefix of the output directory for each pass.

To run unblocked mapReduce, run mapReduce.jar; to run blocked mapReduce run mapReduceBlocked.jar; and to run random blocked mapReduce run mapReduceBlockedRandom.jar
The arguments are all the same for each run.

When the algorithum is done, we must post process the data.  Copy the final output dir from amazon and run postProcess.jar outputFile.txt outputDir.
Where outputFile.txt is the file to write the post processed data to
And outputDir is the output directory copied from amazon
