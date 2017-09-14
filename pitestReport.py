import web
from xml.dom import minidom

titleString = "Pitest Statistics Extractor"
versionString = "v0.01"

print (titleString, versionString)

def extractPitestStatistics(xmlString):
	"""Given  an XML string that represents Pitest output.  Extract out the statistics"""
	xmldoc = minidom.parse('./build/reports/pitest/mutations.xml')
	mutationList = xmldoc.getElementsByTagName('mutation')

	mutations = len(mutationList)
	killed = 0
	survived = 0
	no_coverage = 0
	timed_out = 0
	
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
	
	return mutations, killed, survived, no_coverage, timed_out

mutations, killed, survived, no_coverage, timed_out = extractPitestStatistics("./build/reports/pitest/mutations.xml")
print ("RESULTS:")
print ("\tMutations: ", mutations, "(of which", (mutations-no_coverage), "are covered and", timed_out, "timed out)")
print ("\tKilled/Survived: ", killed, "/", survived)

urls = ()

urls += ('/report(.*)', 'report')


################ Endpoints #######################

#Default
urls += ('/(.*)', 'index')

class index:        
    def GET(self, name):
    	return titleString + " " + versionString

class report:        
    def GET(self, name):
    	"""rest endpoint that show the most recent result"""
    	statsFile = open("mostRecent.stats", "r")
    	lastResultString = statsFile.read()
    	statsFile.close()

    	lastResults = lastResultString.split(",")

    	#return stats
    	web.header('x-pitest-mutations', lastResults[0]) 
    	web.header('x-pitest-mutations-killed', lastResults[1]) 
    	web.header('x-pitest-mutations-survived', lastResults[2]) 
    	web.header('x-pitest-mutations-no-coverage', lastResults[3]) 
    	web.header('x-pitest-mutations-timed-out', lastResults[4]) 

    	resultsFile = open("mostRecent.result", "r")
    	xmlDoc = resultsFile.read()
    	resultsFile.close()
    	return xmlDoc

    def POST(self, name):
    	"""rest endpoint that accept the XML, adds the results to a history"""
    	xmldoc = web.data()
    	##save last input to file
    	resultsFile = open("mostRecent.result", "w")
    	resultsFile.write(str(xmldoc))
    	resultsFile.close()

    	##save stats to file
    	mutations, killed, survived, no_coverage, timed_out = extractPitestStatistics(xmldoc)
    	statsEntry = str(mutations) +","+ str(killed) +","+ str(survived) +","+ str(no_coverage) +","+ str(timed_out)
    	statsFile = open("mostRecent.stats", "w")
    	statsFile.write(statsEntry)
    	statsFile.close()

    	#return stats
    	web.header('x-pitest-mutations', mutations) 
    	web.header('x-pitest-mutations-killed', killed) 
    	web.header('x-pitest-mutations-survived', survived) 
    	web.header('x-pitest-mutations-no-coverage', no_coverage) 
    	web.header('x-pitest-mutations-timed-out', timed_out) 
    	return 

############### Entry Point #######################
if __name__ == "__main__": 
    app = web.application(urls, globals())
    app.run()   
