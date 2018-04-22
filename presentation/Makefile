# In The Name Of God
# ========================================
# [] File Name : Makefile
#
# [] Creation Date : 15-01-2016
#
# [] Created By : Parham Alvani (parham.alvani@gmail.com)
# =======================================

.PHONY: all
all: main

main: main.tex fonts
	xelatex main.tex
	bibtex main
	xelatex main.tex
	xelatex main.tex
