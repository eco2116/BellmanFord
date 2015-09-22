JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Client.java \
	DistanceVector.java \
	Node.java \
	NodeWeight.java \
	FileHelper.java \
	Message.java \
	LinkMessage.java \
	ApplicationMessage.java \
	NetworkMessage.java \
	DataPacket.java \

default: classes
classes: $(CLASSES:.java=.class)
clean:
	$(RM) *.class
