# Makefile for compiling specific Java files and packaging them into a JAR file

# Variables
JAVAC = javac
JFLEX = jflex
PDFLATEX = pdflatex
JAR = jar
# PWD = $(shell pwd) # Doesn't always work since full path might contain spaces
PWD = .
SRC_DIR = src
PACKAGE_DIR = ${SRC_DIR} # Unused

DIST_DIR = ${PWD}/dist
MAIN_CLASS = ${SRC_DIR}/Main  # Update this if your main class is different

JAR_NAME = $(DIST_DIR)/part1.jar

TEST_DIR = ${PWD}/test
TEST_FILE ?= ${TEST_DIR}/Euclid.gls

DOC_DIR = ${PWD}/doc
LATEX_FILE = ${DOC_DIR}/latex/Report.tex

# Targets
all: build jar

build:
	${JFLEX} ${SRC_DIR}/LexicalAnalyzer.flex
	${JAVAC} -cp ${SRC_DIR} ${SRC_DIR}/LexicalAnalyzer.java
	${JAVAC} -cp ${SRC_DIR} ${SRC_DIR}/Main.java

jar:
	${JAR} cvfm ${JAR_NAME} ${SRC_DIR}/manifest.mf -C ${SRC_DIR} .

pdf:
	${PDFLATEX} -output-directory=${DOC_DIR} ${LATEX_FILE}
	rm -f ${DOC_DIR}/*.aux
	rm -f ${DOC_DIR}/*.log
	rm -f ${DOC_DIR}/*.out
	rm -f ${DOC_DIR}/*.toc

.PHONY: all build jar pdf test

test:
	@java -jar ${JAR_NAME} ${TEST_FILE}

clean:
	rm -f ${SRC_DIR}/*.class
	rm -f ${SRC_DIR}/LexicalAnalyzer.java
	rm -f ${SRC_DIR}/LexicalAnalyzer.java~
	rm -f ${JAR_NAME}
	rm -f ${DOC_DIR}/latex/*.aux
	rm -f ${DOC_DIR}/latex/*.bbl
	rm -f ${DOC_DIR}/latex/*.blg
	rm -f ${DOC_DIR}/latex/*.log
	rm -f ${DOC_DIR}/latex/*.out
	rm -f ${DOC_DIR}/latex/*.synctex.gz
	rm -f ${DOC_DIR}/latex/*.lot
	rm -f ${DOC_DIR}/latex/*.toc