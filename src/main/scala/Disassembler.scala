import java.io.FileWriter

import scala.collection.mutable

/*
  Chip8/SuperCHIP Disassembler.

  Copyright (C) 2018 Francesco Rigoni - francesco.rigoni@gmail.com
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License v3 as published by
  the Free Software Foundation.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import Implicits._

class Disassembler(private val programBytes: Array[Byte],
                   private val segments: mutable.Map[Short, Int],
                   private val outputAsmFileName: String,
                   private val outputDataFileName: String) extends InstructionDecode {

  val disassembledCodeOutput = new StringBuilder()
  val disassembledAddresses = new mutable.HashSet[Short]()

  def disassemble(): Unit = {
    val outputAsmFileWriter = new FileWriter(outputAsmFileName)

    for (segmentAddress <- segments.keySet.toSeq.sorted) {
      // Opcode at this address might already have been disassembled
      if (!disassembledAddresses.contains(segmentAddress)) {
        disassembledCodeOutput
          .append(asSegment(segmentAddress))
          .append("\n\n")

        val instructionsCount = segments.get(segmentAddress)
        var currentAddress = segmentAddress

        for (_ <- 0 until instructionsCount.get) {
          val highByte = programBytes(currentAddress - Constants.PROGRAM_START)
          val lowByte = programBytes((currentAddress - Constants.PROGRAM_START) + 1)
          val instruction = ((highByte << 8) & 0xFF00) | (lowByte & 0xFF)

          disassembledCodeOutput.append("Addr ")
          disassembledCodeOutput.append(asHexAddress(currentAddress))
          disassembledCodeOutput.append(" Opcode ")
          disassembledCodeOutput.append(asOpcode(instruction))
          disassembledCodeOutput.append(" ")
          decode(instruction)
          disassembledCodeOutput.append("\n")

          disassembledAddresses.add(currentAddress)
          currentAddress += 2
        }

        disassembledCodeOutput.append("\n\n")
      }
    }

    disassembledCodeOutput.append("=== Data ===\n\n")
    for (address <- programBytes.indices) {
      val chip8Address = address + Constants.PROGRAM_START
      if (!disassembledAddresses.contains(chip8Address)) {
        disassembledCodeOutput.append("Addr ")
        disassembledCodeOutput.append(asHexAddress(chip8Address))
        disassembledCodeOutput.append(" Hex ")
        disassembledCodeOutput.append(asHexByte(programBytes(address)))
        disassembledCodeOutput.append(" Bin ")
        disassembledCodeOutput.append(
          String.format("%8s", Integer.toBinaryString(programBytes(address) & 0xFF)).replace(' ', '0'))
        disassembledCodeOutput.append("\n")
      }
    }

    outputAsmFileWriter.write(disassembledCodeOutput.toString())
    outputAsmFileWriter.close()
  }

  private def asHexByte(x: Byte): String = f"$x%02X"
  private def asHexString(x: Short): String = f"$x%X"
  private def asHexAddress(x: Int): String = f"$x%04X"
  private def asSegment(x: Int): String = ".segment_at_" + f"$x%04X"
  private def asOpcode(x: Int): String = f"$x%04X"

  override def ret(): Unit = {
    disassembledCodeOutput.append("RET")
  }

  override def cls(): Unit = {
    disassembledCodeOutput.append("CLS")
  }

  override def skp_Vx(x: Int): Unit = {
    disassembledCodeOutput.append("SKP V").append(asHexString(x))
  }

  override def sknp_Vx(x: Int): Unit = {
    disassembledCodeOutput.append("SKNP V").append(asHexString(x))
  }

  override def ld_Vx_DT(x: Int): Unit = {
    disassembledCodeOutput.append("LD V").append(asHexString(x)).append(", DT")
  }

  override def ld_DT_Vx(x: Int): Unit = {
    disassembledCodeOutput.append("LD DT, V").append(asHexString(x))
  }

  override def ld_Vx_K(x: Int): Unit = {
    disassembledCodeOutput.append("LD V").append(asHexString(x)).append(", K")
  }

  override def ld_ST_Vx(x: Int): Unit = {
    disassembledCodeOutput.append("LD ST, V").append(asHexString(x))
  }

  override def add_I_Vx(x: Int): Unit = {
    disassembledCodeOutput.append("ADD I, V").append(asHexString(x))
  }

  override def ld_F_Vx_8_by_5(x: Int): Unit = {
    disassembledCodeOutput.append("LD F, V").append(asHexString(x))
  }

  override def ld_B_Vx(x: Int): Unit = {
    disassembledCodeOutput.append("LD B, V").append(asHexString(x))
  }

  override def ld_I_Vx(x: Int): Unit = {
    disassembledCodeOutput.append("LD [I], V").append(asHexString(x))
  }

  override def ld_Vx_I(x: Int): Unit = {
    disassembledCodeOutput.append("LD V").append(asHexString(x)).append(", [I]")
  }

  override def ld_Vx_Vy(xy: (Int, Int)): Unit = {
    disassembledCodeOutput.append("LD V").append(asHexString(xy._1)).append(", V").append(asHexString(xy._2))
  }

  override def or_Vx_Vy(xy: (Int, Int)): Unit = {
    disassembledCodeOutput.append("OR V").append(asHexString(xy._1)).append(", V").append(asHexString(xy._2))
  }

  override def and_Vx_Vy(xy: (Int, Int)): Unit = {
    disassembledCodeOutput.append("OR V").append(asHexString(xy._1)).append(", V").append(asHexString(xy._2))
  }

  override def xor_Vx_Vy(xy: (Int, Int)): Unit = {
    disassembledCodeOutput.append("XOR V").append(asHexString(xy._1)).append(", V").append(asHexString(xy._2))
  }

  override def add_Vx_Vy(xy: (Int, Int)): Unit = {
    disassembledCodeOutput.append("ADD V").append(asHexString(xy._1)).append(", V").append(asHexString(xy._2))
  }

  override def sub_Vx_Vy(xy: (Int, Int)): Unit = {
    disassembledCodeOutput.append("SUB V").append(asHexString(xy._1)).append(", V").append(asHexString(xy._2))
  }

  override def sub_Vy_Vx(xy: (Int, Int)): Unit = {
    disassembledCodeOutput.append("SUBN V").append(asHexString(xy._1)).append(", V").append(asHexString(xy._2))
  }

  override def shr_Vx(xy: (Int, Int)): Unit = {
    disassembledCodeOutput.append("SHR V").append(asHexString(xy._1)).append(" {, V").append(asHexString(xy._2)).append("}")
  }

  override def shl_Vx(xy: (Int, Int)): Unit = {
    disassembledCodeOutput.append("SHL V").append(asHexString(xy._1)).append(" {, V").append(asHexString(xy._2)).append("}")
  }

  override def sne_Vx_Vy(xy: (Int, Int)): Unit = {
    disassembledCodeOutput.append("SNE V").append(asHexString(xy._1)).append(", V").append(asHexString(xy._2))
  }

  override def se_Vx_Vy(xy: (Int, Int)): Unit = {
    disassembledCodeOutput.append("SE V").append(asHexString(xy._1)).append(", V").append(asHexString(xy._2))
  }

  override def jp_nnn(nnn: Short): Unit = {
    disassembledCodeOutput.append("JP ").append(asHexAddress(nnn))

  }

  override def call_nnn(nnn: Short): Unit = {
    disassembledCodeOutput.append("CALL ").append(asHexAddress(nnn))
  }

  override def sne_Vx_kk(xkk: (Int, Int)): Unit = {
    disassembledCodeOutput.append("SNE V").append(asHexString(xkk._1)).append(", ").append(xkk._2)
  }

  override def se_Vx_kk(xkk: (Int, Int)): Unit = {
    disassembledCodeOutput.append("SE V").append(asHexString(xkk._1)).append(", ").append(xkk._2)
  }

  override def ld_Vx_kk(xkk: (Int, Int)): Unit = {
    disassembledCodeOutput.append("LD V").append(asHexString(xkk._1)).append(", ").append(xkk._2)
  }

  override def add_Vx_kk(xkk: (Int, Int)): Unit = {
    disassembledCodeOutput.append("ADD V").append(asHexString(xkk._1)).append(", ").append(xkk._2)
  }

  override def ld_I_nnn(nnn: Short): Unit = {
    disassembledCodeOutput.append("LD I, ").append(asHexString(nnn))
  }

  override def jp_V0_nnn(nnn: Short): Unit = {
    disassembledCodeOutput.append("JP V0, ").append(asHexString(nnn)).append(" !!! UNKNOWN DESTINATION !!!")
  }

  override def rnd_Vx_kk(xkk: (Int, Int)): Unit = {
    disassembledCodeOutput.append("RND V").append(asHexString(xkk._1)).append(", ").append(asHexString(xkk._2))
  }

  override def drw_Vx_Vy_n(xyn: (Int, Int, Int)): Unit = {
    disassembledCodeOutput.append("DRW V").append(asHexString(xyn._1)).append(", V")
      .append(asHexString(xyn._2)).append(", ").append(asHexString(xyn._3))
  }

  override def unimplemented: Unit = {
    disassembledCodeOutput.append("End of segment")
  }

  override def ld_r_Vx(x: Int): Unit = {
    disassembledCodeOutput.append("LD R, V").append(asHexString(x))
  }

  override def ld_Vx_r(x: Int): Unit = {
    disassembledCodeOutput.append("LD V").append(asHexString(x)).append(", R")
  }

  override def ld_F_Vx_8_by_10(x: Int): Unit = {
    disassembledCodeOutput.append("LD HF, V").append(asHexString(x))
  }

  override def scr(): Unit = {
    disassembledCodeOutput.append("SCR")
  }

  override def scl(): Unit = {
    disassembledCodeOutput.append("SCL")
  }

  override def exit(): Unit = {
    disassembledCodeOutput.append("EXIT")
  }

  override def high_res_on(enabled: Boolean): Unit = {
    disassembledCodeOutput.append(if (enabled) "HIGH" else "LOW")
  }

  override def scu_n(n: Int): Unit = {
    disassembledCodeOutput.append("SCU ").append(n)
  }

  override def scd_n(n: Int): Unit = {
    disassembledCodeOutput.append("SCD ").append(n)
  }
}