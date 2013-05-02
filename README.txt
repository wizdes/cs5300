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
