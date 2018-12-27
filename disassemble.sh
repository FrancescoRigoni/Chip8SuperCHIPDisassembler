#!/bin/bash

romPath=`echo $1 | sed s/\\ /\ /g`
echo "Disassembling $romPath"
sbt "run $romPath"