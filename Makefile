# Makefile for compiling specific Java files and packaging them into a JAR file

# Variables
JAVAC = javac
JFLEX = jflex
PDFLATEX = pdflatex
JAR = jar
PWD = $(shell pwd)
SRC_DIR = src
PACKAGE_DIR = ${SRC_DIR} # Unused
DIST_DIR = ${PWD}/dist
MAIN_CLASS = ${SRC_DIR}/Main  # Update this if your main class is different
JAR_NAME = $(DIST_DIR)/part1.jar
TEST_DIR = ${PWD}/test
TEST_FILE = Random.gls

all: build jar

build:
	${JFLEX} ${SRC_DIR}/LexicalAnalyzer.flex
	${JAVAC} -cp ${SRC_DIR} ${SRC_DIR}/LexicalAnalyzer.java
	${JAVAC} -cp ${SRC_DIR} ${SRC_DIR}/Main.java
	${PDFLATEX} -output-directory=./doc/ ./doc/latex/document.tex

jar:
	${JAR} cvfm ${JAR_NAME} ${SRC_DIR}/manifest.txt -C ${SRC_DIR} .

.PHONY: all build jar test

test:
	@java -jar ${JAR_NAME} ${TEST_DIR}/${TEST_FILE}
