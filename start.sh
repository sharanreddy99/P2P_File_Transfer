#!/bin/bash
find . -type f -path "./*" -name "*.class" -delete
make
make run
