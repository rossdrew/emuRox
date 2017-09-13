
from xml.dom import minidom

print ("Pitest Statistics Extractor v0.01")

from xml.dom import minidom
xmldoc = minidom.parse('./build/reports/pitest/mutations.xml')
mutationList = xmldoc.getElementsByTagName('mutation')

mutations = len(mutationList)

killed = 0
survived = 0
no_coverage = 0
timed_out = 0
# print(mutationList[0].attributes['name'].value)
for mutation in mutationList:
    status = mutation.attributes['status'].value
    if status == "KILLED":
    	killed = killed + 1
    elif status == "SURVIVED":
    	survived = survived + 1
    elif status == "NO_COVERAGE":
    	no_coverage = no_coverage + 1
    elif status == "TIMED_OUT":
    	timed_out = timed_out + 1


print ("RESULTS:")
print ("\tMutations: ", mutations, "(of which", (mutations-no_coverage), "are covered and", timed_out, "timed out)")
print ("\tKilled/Survived: ", killed, "/", survived)

#TODO Add a rest endpoint that accept the XML, adds the results to a history
#TODO Add a rest endpoint that show the most recent result

