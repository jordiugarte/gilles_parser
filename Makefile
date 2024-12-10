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
TEST_FILE ?= ${TEST_DIR}/ComplexAssignment.gls

#OUTPUT_TEX_FILE ?= ${MORE_DIR}/parseTreeOutput.tex
OUTPUT_TEX_FILENAME = parseTreeOutput.tex

DOC_DIR = ${PWD}/doc

# Targets
all: build jar

build:
	@echo "Building JFlex and Java files..."
	@${JFLEX} ${SRC_DIR}/LexicalAnalyzer.flex
	@${JAVAC} -cp ${SRC_DIR} ${SRC_DIR}/LexicalAnalyzer.java
	@${JAVAC} -cp ${SRC_DIR} ${SRC_DIR}/Main.java
	@echo "Build successful!"

jar:
	@echo "Creating JAR file..."
	@${JAR} cvfm ${JAR_NAME} ${SRC_DIR}/manifest.mf -C ${SRC_DIR} .
	@echo "JAR file created!"

.PHONY: all build jar test

test:
	@java -jar ${JAR_NAME} $(if ${OUTPUT_TEX_FILE},-wt ${OUTPUT_TEX_FILE}) ${TEST_FILE}

	@# If there was a .tex file specified, then generate the LaTeX and PDF files, else we're done!
	@$(if ${OUTPUT_TEX_FILE}, $(PDFLATEX) -output-directory=$(MORE_DIR) $(OUTPUT_TEX_FILE) > /dev/null 2>&1)
	@#@$(if ${OUTPUT_TEX_FILE}, mv $(MORE_DIR)/$(basename $(notdir ${OUTPUT_TEX_FILE})).pdf $(DOC_DIR)/$(basename $(notdir ${OUTPUT_TEX_FILE}))_part2.pdf)
	@#$(if ${OUTPUT_TEX_FILE}, @echo "Parse tree saved to $(DOC_DIR)/$(basename $(notdir ${OUTPUT_TEX_FILE}))_part3.pdf")

clean:
	# Remove all .class and auto-generated files
	rm -f ${SRC_DIR}/*.class
	rm -f ${SRC_DIR}/LexicalAnalyzer.java
	rm -f ${SRC_DIR}/LexicalAnalyzer.java~

	# Remove the output JAR file
	rm -f ${JAR_NAME}

	# Remove files generated in MORE_DIR that get generated during execution
	rm -f ${MORE_DIR}/*

	# Remove all PDFs except the report.pdf (parse tree PDFs)
	find ${DOC_DIR} -type f -name '*.pdf' ! -name 'Report.pdf' -exec rm -f {} +