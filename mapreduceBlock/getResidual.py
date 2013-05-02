import sys

f0 = open("ourFormat.txt").readlines()
f1 = open("output0/part-00000").readlines()

keyf0 = {}
keyf1 = {}

diff = 0
for index, elt in enumerate(f0):
	param = elt.split()
	if(param[0] in keyf0): continue
	keyf0[param[0]] = float(param[1])

sumNew = 0
for elt in f1:
	param = elt.split()
	if(param[0] not in keyf1): 
		oneDiff = keyf0[param[0]] - float(param[1])
		if oneDiff < 0: oneDiff = oneDiff * -1
		oneDiff = oneDiff / float(param[1])
		print "Diff:" + str(keyf0[param[0]]) + " " + str(float(param[1])) + " " + str(oneDiff)
		diff += oneDiff
		#sumNew += float(param[1])
		keyf1[param[0]] = 1

print diff
print sumNew
residual = diff * 1.0/len(keyf0)
print residual
