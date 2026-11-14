#include "TerseBlockReader.h"
#include "Constants.h"
#include <stdexcept>

TerseBlockReader::TerseBlockReader(std::istream &input): input_(input) {}

int TerseBlockReader::getBlok()
{
  // If there are no leftover bits, read 2 bytes => 12 bits + leftover 4 bits
  if (bitsAvailable_ == 0)
  {
    char byte1 = 0;
    if (!input_.get(byte1))
    {
      if (input_.bad() || !input_.eof())
      {
        throw std::runtime_error("Failed to read TERSE input");
      }
      return Tersedecompress::ENDOFFILE;
    }
    unsigned char b1 = static_cast< unsigned char >(byte1);

    char byte2 = 0;
    if (!input_.get(byte2))
    {
      // We got 1 byte, but not the second => partial data => throw
      throw std::runtime_error("Tried to read 12 bits, found EOF after reading 8 bits");
    }
    unsigned char b2 = static_cast< unsigned char >(byte2);

    // savedBits_ is the low 4 bits of b2
    savedBits_ = b2 & 0x0F;
    bitsAvailable_ = 4;

    // Return the top 8 bits from b1 plus top 4 bits from b2 => 12 bits
    //  b1 goes in the upper 8 bits, (b2 >> 4) in the lower 4 bits
    int result = (static_cast< int >(b1) << 4) | (static_cast< int >(b2) >> 4);
    return result;
  }
  else
  {
    // We have leftover 4 bits in savedBits_. Need 8 more bits from next byte.
    if (bitsAvailable_ != 4)
    {
      // Should never happen, but let's be safe.
      throw std::runtime_error("Unexpected bitsAvailable_ != 0 or 4");
    }

    char byte2 = 0;
    if (!input_.get(byte2))
    {
      if (input_.bad() || !input_.eof())
      {
        throw std::runtime_error("Failed to read TERSE input");
      }
      // We have leftover 4 bits, but no new byte => assume this is EOF,
      // meaning the leftover bits were "padding". Return ENDOF_FILE.
      return Tersedecompress::ENDOFFILE;
    }
    unsigned char b2 = static_cast< unsigned char >(byte2);

    // Now we combine leftover 4 bits from savedBits_ with the 8 bits in b2 => 12 bits
    int result = (savedBits_ << 8) | static_cast< int >(b2);

    // reset leftover bits
    bitsAvailable_ = 0;
    savedBits_ = 0;

    return result;
  }
}
