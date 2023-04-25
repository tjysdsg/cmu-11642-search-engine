all:
ifeq ($(OS),Windows_NT)
	# assume windows
	# javac -Xlint -cp ".;lucene-8.1.1/*;RankLib/*" -g *.java
	javac -Xlint -cp ".;lucene-8.1.1/*" -g *.java
else
	# assume Linux
	# javac -Xlint -cp ".:lucene-8.1.1/*:RankLib/*" -g *.java
	javac -Xlint -cp ".:lucene-8.1.1/*" -g *.java
endif
