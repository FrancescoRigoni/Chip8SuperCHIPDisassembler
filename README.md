Chip8/SuperChip/Chip48 Disassembler
====================================
A disassembler for Chip8/Chip48/SuperChip roms written in Scala.
I needed this while working on my Chip8/SuperCHIP emulator, then I made it independent and reusable.

To build the disassembler you need to make sure you have `scalac` and `sbt`, then run:
```
$ sbt compile
```
You can also import the project in IntelliJ.
To disassemble a rom use the `disassemble.sh` script
```
$ ./disassemble.sh Blinky\ \[Hans\ Christian\ Egeberg\,\ 1991\].ch8
```
The output will be a file named `Blinky [Hans Christian Egeberg, 1991].ch8.asm` which contains the disassembled code and the disassembled sprite data.
An example of disassembled code:
```
.segment_at_0200

Addr 0200 Opcode 121A JP 021A

.segment_at_021A

Addr 021A Opcode 00FF HIGH
Addr 021C Opcode 8003 XOR V0, V0
Addr 021E Opcode 8113 XOR V1, V1
Addr 0220 Opcode A8E2 LD I, 8E2
Addr 0222 Opcode F155 LD [I], V1
Addr 0224 Opcode 6005 LD V0, 5
Addr 0226 Opcode A8E6 LD I, 8E6
Addr 0228 Opcode F055 LD [I], V0
Addr 022A Opcode 8773 XOR V7, V7
Addr 022C Opcode 8663 XOR V6, V6
Addr 022E Opcode 2786 CALL 0786
Addr 0230 Opcode 00E0 CLS
```
An example of disassembled data for a sprite:
```
Addr 0929 Hex 1C Bin 00011100
Addr 092A Hex 3E Bin 00111110
Addr 092B Hex 49 Bin 01001001
Addr 092C Hex 77 Bin 01110111
Addr 092D Hex 7F Bin 01111111
Addr 092E Hex 63 Bin 01100011
Addr 092F Hex 7F Bin 01111111
```
Notes
------
The disassembler works in three steps
* Figure out which addresses in memory are code segments. Chip8/SuperCHIP roms do not provide any kind of informations so this is achieved with a breadth first algorithm by collecting and analyzing the destination addresses of CALLs and JPs (Jumps).
* Disassemble each code segment.
* Any address that was not disassembled as code gets disassembled as data.

Please note that this is highly experimental and speculative coding on my side. This disassembler did the job for me, however it is not perfect.
One known issue is with the opcode `JP V0, nnn` which jumps to a location specified by register V0 plus nnn. The disassembler is static, it does not keep track of what is happening with the registers. Therefore segments of code referenced by this opcode cannot be traced.
As a result some code segments might get disassembled as data. However I did not find many roms using this opcode, those that do not should be disassembled just fine.

I found a bug
---
Let me know! Or send me a PR please.

[The Code Butchery](https://thecodebutchery.com)


