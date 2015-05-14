# Author: Yuepeng Wang
# UT EID: yw7284
# Email : ypwang@cs.utexas.edu

JAVA		= java
JAVAC		= javac
CLASSPATH	= "lib/edu.mit.jwi_2.3.3_jdk.jar:."
JAVAFLAGS	= -cp $(CLASSPATH)

SOURCE		= src/edu/utexas/crawler/*.java \
		  src/edu/utexas/seg/*.java \
		  src/edu/utexas/wordnet/*.java \
		  src/org/tartarus/snowball/*.java \
		  src/org/tartarus/snowball/ext/englishStemmer.java \
		  src/uk/ac/man/cs/choif/extend/io/*.java \
		  src/uk/ac/man/cs/choif/nlp/doc/basic/*.java \
		  src/uk/ac/man/cs/choif/nlp/surface/*.java \

#		  src/uk/ac/man/cs/choif/extend/io/*.java \
		  src/uk/ac/man/cs/choif/extend/structure/*.java \
		  src/uk/ac/man/cs/choif/nlp/dictionary/*.java \
		  src/uk/ac/man/cs/choif/nlp/doc/*.java \
		  src/uk/ac/man/cs/choif/nlp/doc/basic/*.java \
		  src/uk/ac/man/cs/choif/nlp/entity/*.java \
		  src/uk/ac/man/cs/choif/nlp/location/*.java \
		  src/uk/ac/man/cs/choif/nlp/parse/*.java \
		  src/uk/ac/man/cs/choif/nlp/pos/*.java \
		  src/uk/ac/man/cs/choif/nlp/seg/linear/similarity/*.java \

all:
	$(JAVAC) $(JAVAFLAGS) $(SOURCE)

#	$(JAVAC) -classpath $(CLASSPATH) edu.utexas.seg.*
#	$(JAVA) $(JAVAFLAGS) ActiveLearning

clean:
	$(RM) *.class

