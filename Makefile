JAVAC=javac
JAVADOC=javadoc
JAR=jar


MAIN_CLASS=Belka.java
APPLET_CLASS=BelkaApplet.java
MANIFEST_FILE=BelkaManifest
JAR_FILE=Belka.jar
CLASS_DIR=classes
DOC_DIR=doc
JAVA_API_URL=http://java.sun.com/j2se/1.5.0/docs/api/

all: prepare compile jar_all

jar_all:
	$(JAR) cmf $(MANIFEST_FILE) $(JAR_FILE) -C $(CLASS_DIR) .

compile:
	@echo "Compiling Belka"
	$(JAVAC) -d $(CLASS_DIR) $(MAIN_CLASS)
	$(JAVAC) -d $(CLASS_DIR) $(APPLET_CLASS)

prepare:
	mkdir -p $(CLASS_DIR)

clean:
	@rm -rf $(CLASS_DIR) belka/*.class belka/*/*.class belka/*/*/*.class belka/*/*/*/*.class

libs: 
	@echo "Compiling library exPDB"
	rm -f exPDB/*.class
	$(JAVAC) -classpath $(JAR_FILE) exPDB/exPDB.java
	$(JAR) cf exPDB.jar exPDB/
	@echo "Compiling library rfAux"
	rm -f rfAux/*.class
	$(JAVAC) -classpath $(JAR_FILE) rfAux/rfAux.java
	$(JAR) cf rfAux.jar rfAux/
	@echo "Compiling library libExample"
	rm -f libExample/*.class
	$(JAVAC) -classpath $(JAR_FILE) libExample/libExample.java
	$(JAR) cf libExample.jar libExample

html:
#	$(JAVADOC) -author -d $(DOC_DIR) -link $(JAVA_API_URL) belka belka.chem belka.draw belka.mol belka.parser belka.menu belka.align belka.geom $(MAIN_CLASS) $(APPLET_CLASS)
	$(JAVADOC) -author -d $(DOC_DIR) -link $(JAVA_API_URL) -subpackages belka:Jama $(MAIN_CLASS) $(APPLET_CLASS)
