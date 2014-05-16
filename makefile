JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        ID3Mining.java \
        AVList.java \
        DataBase.java \
				Target.java \
				Node.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
