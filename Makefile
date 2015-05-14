# Author: Yuepeng Wang
# UT EID: yw7284
# Email : ypwang@cs.utexas.edu

JAVA		= java
JAVAC		= javac
BIN_DIR		= bin
CLASSPATH	= "lib/edu.mit.jwi_2.3.3_jdk.jar:lib/stanford-postagger-3.5.2.jar:bin/:."
JAVAFLAGS	= -cp $(CLASSPATH)

SOURCE		= src/edu/utexas/crawler/*.java \
		  src/edu/utexas/wordnet/*.java \
		  src/org/tartarus/snowball/*.java \
		  src/org/tartarus/snowball/ext/englishStemmer.java \
		  src/edu/utexas/seg/ReviewReader.java \
		  src/edu/utexas/seg/TileFeature.java \
		  src/edu/utexas/seg/TileFeatureGen.java \

PARSE_IN	= data/Office.html
PARSE_OUT	= data/Office.txt
F_IN		= review.txt
F_OUT		= mallet.txt
F_SEL		= tag

all: compile feature

compile:
	$(JAVAC) -d $(BIN_DIR) $(JAVAFLAGS) $(SOURCE)

feature:
	$(JAVA) $(JAVAFLAGS) \
		edu.utexas.seg.TileFeatureGen $(F_IN) $(F_OUT) $(F_SEL)

parser:
	$(JAVA) $(JAVAFLAGS) \
		edu.utexas.crawler.ParseHtml $(PARSE_IN) $(PARSE_OUT)

clean:
	$(RM) *.class

