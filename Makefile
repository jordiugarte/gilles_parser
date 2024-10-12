# Makefile for compiling specific Java files and packaging them into a JAR file

# Variables
JAVAC = javac
JFLEX = jflex
JAR = jar
SRC_DIR = src/
PACKAGE_DIR = ${SRC_DIR}com/gilles/
DIST_DIR = dist
MAIN_CLASS = ${SRC_DIR}Main  # Update this if your main class is different
JAR_NAME = $(DIST_DIR)/part1.jar

all:
	${JFLEX} ./${PACKAGE_DIR}LexicalAnalyzer.flex
	${JAVAC} ./${PACKAGE_DIR}LexicalUnit.java
	${JAVAC} ./${PACKAGE_DIR}Main.java
	${JAR} cvfm ${JAR_NAME} ${PACKAGE_DIR}/manifest.txt ${PACKAGE_DIR}*.class
