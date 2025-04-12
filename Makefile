JAVAC = javac
JAVA = java
SRC_DIR = src
SRC = $(wildcard $(SRC_DIR)/*.java)
CLASS_DIR = build
MAIN_CLASS = MinimalMusicPlayer

all: run

compile:
	mkdir -p $(CLASS_DIR)
	$(JAVAC) -d $(CLASS_DIR) $(SRC)

run: compile
	_JAVA_AWT_WM_NONREPARENTING=1 $(JAVA) -cp $(CLASS_DIR) $(MAIN_CLASS)

clean:
	rm -rf $(CLASS_DIR)
