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
MORE_DIR = ${PWD}/more
MAIN_CLASS = ${SRC_DIR}/Main  # Update this if your main class is different

JAR_NAME = $(DIST_DIR)/part2.jar

TEST_DIR = ${PWD}/test
TEST_FILE ?= ${TEST_DIR}/Euclid.gls

#OUTPUT_TEX_FILE ?= ${MORE_DIR}/parseTreeOutput.tex
OUTPUT_TEX_FILENAME = parseTreeOutput.tex

DOC_DIR = ${PWD}/doc

# Targets
all: build jar

build:
	${JFLEX} ${SRC_DIR}/LexicalAnalyzer.flex
	${JAVAC} -cp ${SRC_DIR} ${SRC_DIR}/LexicalAnalyzer.java
	${JAVAC} -cp ${SRC_DIR} ${SRC_DIR}/Main.java

jar:
	${JAR} cvfm ${JAR_NAME} ${SRC_DIR}/manifest.mf -C ${SRC_DIR} .

.PHONY: all build jar test

#test:
#	@java -jar ${JAR_NAME} -wt ${OUTPUT_TEX_FILE} ${TEST_FILE}
#	@$(PDFLATEX) -output-directory=$(MORE_DIR) $(OUTPUT_TEX_FILE) > /dev/null 2>&1
#	@mv ${MORE_DIR}/parseTreeOutput.pdf ${DOC_DIR}/parseTreeOutput_part2.pdf
#	@echo "Parse tree saved to $(DOC_DIR)/parseTreeOutput_part2.pdf"

test:
	@java -jar ${JAR_NAME} $(if ${OUTPUT_TEX_FILE},-wt ${OUTPUT_TEX_FILE}) ${TEST_FILE}
	@$(if ${OUTPUT_TEX_FILE}, $(PDFLATEX) -output-directory=$(MORE_DIR) $(OUTPUT_TEX_FILE) > /dev/null 2>&1)
	@$(if ${OUTPUT_TEX_FILE}, mv $(MORE_DIR)/$(basename $(notdir ${OUTPUT_TEX_FILE})).pdf $(DOC_DIR)/$(basename $(notdir ${OUTPUT_TEX_FILE}))_part2.pdf)
	$(if ${OUTPUT_TEX_FILE}, @echo "Parse tree saved to $(DOC_DIR)/$(basename $(notdir ${OUTPUT_TEX_FILE}))_part2.pdf")

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
