import sys

lines = open("blocks.txt").readlines()
output = ""
for elt in lines:
	elt = elt.strip() + ","
	output += elt
print output
