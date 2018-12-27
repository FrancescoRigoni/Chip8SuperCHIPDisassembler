
import scala.collection.mutable

/*
  SuperChip Emulator.

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

class ProgramSegment(val startAddress:Short, val instructionsCount: Int)

class SegmentsCollector(private val programBytes: Array[Byte]) extends InstructionDecode {
  // Set of addresses to visit, initially only contains the program start address
  private var addressesToVisit = mutable.HashSet[Short](Constants.PROGRAM_START)
  // Set of already visited addresses
  private val visitedAddresses = mutable.HashSet[Short]()
  // Unimplemented opcode ends the visit to the current segment
  private var unimplementedOpCodeFound = false
  // The current opcode is conditional (e.g. the previous opcode was "Skip next IF" or similar)
  private var conditionalOpCode = false
  // An unconditional jump or return ends the current visit
  private var unconditionalJumpRetFound = false
  // Map of code segments found. Key is address, value is number of instructions
  private val segments = mutable.HashMap[Short, Int]()

  def collect(): Unit = {
    try {
      while (addressesToVisit.nonEmpty) {
        // Get next address to be visited
        val nextAddressToVisit = addressesToVisit.head
        // Remove it from addressesToVisit
        addressesToVisit = addressesToVisit.tail
        // Add it to already visited addresses
        visitedAddresses.add(nextAddressToVisit)
        // Go for it
        visit(nextAddressToVisit)
      }
    }
  }

  def getSegments() : mutable.Map[Short, Int] = segments.clone()

  private def visit(address: Short) = {
    var branchAddress = address

    // Reset all states
    conditionalOpCode = false
    unimplementedOpCodeFound = false
    unconditionalJumpRetFound = false

    while (!unimplementedOpCodeFound &&
           !unconditionalJumpRetFound &&
           (branchAddress - Constants.PROGRAM_START) < programBytes.length) {

      val highByte = programBytes(branchAddress - Constants.PROGRAM_START)
      val lowByte = programBytes((branchAddress - Constants.PROGRAM_START) + 1)
      val instruction = ((highByte << 8) & 0xFF00) | (lowByte & 0xFF)
      decode(instruction)
      branchAddress += 2
    }

    segments.put(address, (branchAddress-address) / 2)
  }

  private def addIfNotVisited(nnn: Short): Unit =
    if (!visitedAddresses.contains(nnn)) {
      addressesToVisit.add(nnn)
    }

  override def ret(): Unit = {
    unconditionalJumpRetFound = !conditionalOpCode
    conditionalOpCode = false
  }

  override def cls(): Unit = {
    conditionalOpCode = false
  }

  override def skp_Vx(x: Int): Unit = {
    conditionalOpCode = true
  }

  override def sknp_Vx(x: Int): Unit = {
    conditionalOpCode = true
  }

  override def ld_Vx_DT(x: Int): Unit = {
    conditionalOpCode = false
  }

  override def ld_DT_Vx(x: Int): Unit = {
    conditionalOpCode = false
  }

  override def ld_Vx_K(x: Int): Unit = {
    conditionalOpCode = false
  }

  override def ld_ST_Vx(x: Int): Unit = {
    conditionalOpCode = false
  }

  override def add_I_Vx(x: Int): Unit = {
    conditionalOpCode = false
  }

  override def ld_F_Vx_8_by_5(x: Int): Unit = {
    conditionalOpCode = false
  }

  override def ld_B_Vx(x: Int): Unit = {
    conditionalOpCode = false
  }

  override def ld_I_Vx(x: Int): Unit = {
    conditionalOpCode = false
  }

  override def ld_Vx_I(x: Int): Unit = {
    conditionalOpCode = false
  }

  override def ld_Vx_Vy(xy: (Int, Int)): Unit = {
    conditionalOpCode = false
  }

  override def or_Vx_Vy(xy: (Int, Int)): Unit = {
    conditionalOpCode = false
  }

  override def and_Vx_Vy(xy: (Int, Int)): Unit = {
    conditionalOpCode = false
  }

  override def xor_Vx_Vy(xy: (Int, Int)): Unit = {
    conditionalOpCode = false
  }

  override def add_Vx_Vy(xy: (Int, Int)): Unit = {
    conditionalOpCode = false
  }

  override def sub_Vx_Vy(xy: (Int, Int)): Unit = {
    conditionalOpCode = false
  }

  override def sub_Vy_Vx(xy: (Int, Int)): Unit = {
    conditionalOpCode = false
  }

  override def shr_Vx(xy: (Int, Int)): Unit = {
    conditionalOpCode = false
  }

  override def shl_Vx(xy: (Int, Int)): Unit = {
    conditionalOpCode = false
  }

  override def sne_Vx_Vy(xy: (Int, Int)): Unit = {
    conditionalOpCode = true
  }

  override def se_Vx_Vy(xy: (Int, Int)): Unit = {
    conditionalOpCode = true
  }

  override def jp_nnn(nnn: Short): Unit = {
    addIfNotVisited(nnn)
    unconditionalJumpRetFound = !conditionalOpCode
    conditionalOpCode = false
  }

  override def call_nnn(nnn: Short): Unit = {
    addIfNotVisited(nnn)
    conditionalOpCode = false
  }

  override def sne_Vx_kk(xkk: (Int, Int)): Unit = {
    conditionalOpCode = true
  }

  override def se_Vx_kk(xkk: (Int, Int)): Unit = {
    conditionalOpCode = true
  }

  override def ld_Vx_kk(xkk: (Int, Int)): Unit = {
    conditionalOpCode = false
  }

  override def add_Vx_kk(xkk: (Int, Int)): Unit = {
    conditionalOpCode = false
  }

  override def ld_I_nnn(nnn: Short): Unit = {
    conditionalOpCode = false
  }

  override def jp_V0_nnn(nnn: Short): Unit = {
    // This is a jump, however there is no way to know where this jumps to using
    // a static approach, so this opcode is not handled and programs using it won't
    // be completely disassembled.
    conditionalOpCode = false
  }

  override def rnd_Vx_kk(xkk: (Int, Int)): Unit = {
    conditionalOpCode = false
  }

  override def drw_Vx_Vy_n(xyn: (Int, Int, Int)): Unit = {
    conditionalOpCode = false
  }

  override def unimplemented: Unit = {
    unimplementedOpCodeFound = true
    conditionalOpCode = false
  }

  override def ld_r_Vx(x: Int): Unit = {
    conditionalOpCode = false
  }

  override def ld_Vx_r(x: Int): Unit = {
    conditionalOpCode = false
  }

  override def ld_F_Vx_8_by_10(x: Int): Unit = {
    conditionalOpCode = false
  }

  override def scr(): Unit = {
    conditionalOpCode = false
  }

  override def scl(): Unit = {
    conditionalOpCode = false
  }

  override def exit(): Unit = {
    conditionalOpCode = false
  }

  override def high_res_on(enabled: Boolean): Unit = {
    conditionalOpCode = false
  }

  override def scu_n(n: Int): Unit = {
    conditionalOpCode = false
  }

  override def scd_n(n: Int): Unit = {
    conditionalOpCode = false
  }
}