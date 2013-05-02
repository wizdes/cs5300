import sys

fromNetID = 0.6872;
rejectMin = 0.99 * fromNetID;
rejectLimit = rejectMin + 0.01;

f0 = open("ourFormat.txt").readlines()

for index, elt in enumerate(f0):
	param = elt.split()
	if(param[0] in keyf0): continue
	keyf0[param[0]] = float(param[1])
