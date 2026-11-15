#include "TerseHeader.h"
#include <cstdio>
#include <stdexcept>
#include <string>

TerseHeader TerseHeader::parseHeader(std::istream &in)
{
  TerseHeader header;

  // Read versionFlag
  {
    char byteVal = 0;
    if (!in.get(byteVal))
    {
      throw std::runtime_error("Failed to read versionFlag");
    }
    header.versionFlag = static_cast< uint8_t >(byteVal);
  }

  switch (header.versionFlag)
  {
    case 0x01: // native binary mode (4-byte header, versions 1.2+)
    // case 0x07: // native binary mode (4-byte header, versions 1.1-)
    {
      // read next 3 bytes => validate 0x89, 0x69, 0xA5
      uint8_t b2, b3, b4;
      {
        char temp = 0;
        if (!in.get(temp))
        {
          throw std::runtime_error("EOF reading native binary header (byte2)");
        }
        b2 = static_cast< uint8_t >(temp);
      }
      {
        char temp = 0;
        if (!in.get(temp))
        {
          throw std::runtime_error("EOF reading native binary header (byte3)");
        }
        b3 = static_cast< uint8_t >(temp);
      }
      {
        char temp = 0;
        if (!in.get(temp))
        {
          throw std::runtime_error("EOF reading native binary header (byte4)");
        }
        b4 = static_cast< uint8_t >(temp);
      }

      // Then read recordLen1 as two bytes (big-endian)
      {
        char temp1, temp2;
        if (!in.get(temp1) || !in.get(temp2))
        {
          throw std::runtime_error("EOF reading recordLen1 in native binary mode");
        }
        header.recordLen1 = (static_cast< uint16_t >(static_cast< unsigned char >(temp1)) << 8) |
                            (static_cast< uint16_t >(static_cast< unsigned char >(temp2)));
      }

      // Validate
      if (b2 != 0x89 || b3 != 0x69 || b4 != 0xA5)
      {
        throw std::runtime_error("Invalid header validation flags in native binary mode");
      }
      // recordLength = recordLen1
      header.recordLength = header.recordLen1;
      break;
    }
    case 0x02: // PACK (12-byte header)
    case 0x05: // SPACK (12-byte header)
    case 0x07: // PACK (12-byte header), PDS or PDSE
    case 0x09: // SPACK (12-byte header), PDS or PDSE
    {
      // variableFlag (1 byte)
      {
        char temp;
        if (!in.get(temp))
        {
          throw std::runtime_error("EOF reading variableFlag");
        }
        if (temp != 0x00 && temp != 0x01)
        {
          throw std::runtime_error("Record format flag not recognized: " + std::to_string(temp));
        }
        header.variableFlag = temp == 0x01;
      }
      // recordLen1 (2 bytes)
      {
        char temp1, temp2;
        if (!in.get(temp1) || !in.get(temp2))
        {
          throw std::runtime_error("EOF reading recordLen1");
        }
        header.recordLen1 = (static_cast< uint16_t >(static_cast< unsigned char >(temp1)) << 8) |
                            (static_cast< uint16_t >(static_cast< unsigned char >(temp2)));
      }
      // flags (1 byte)
      {
        char temp;
        if (!in.get(temp))
        {
          throw std::runtime_error("EOF reading flags");
        }
        header.flags = static_cast< uint8_t >(temp);
      }
      // ratio (1 byte)
      {
        char temp;
        if (!in.get(temp))
        {
          throw std::runtime_error("EOF reading ratio");
        }
        header.ratio = static_cast< uint8_t >(temp);
      }
      // blockSize (2 bytes)
      {
        char temp1, temp2;
        if (!in.get(temp1) || !in.get(temp2))
        {
          throw std::runtime_error("EOF reading blockSize");
        }
        header.blockSize = (static_cast< uint16_t >(static_cast< unsigned char >(temp1)) << 8) |
                           (static_cast< uint16_t >(static_cast< unsigned char >(temp2)));
      }
      // recordLen2 (4 bytes)
      {
        char buf[4];
        if (!in.read(buf, 4))
        {
          throw std::runtime_error("EOF reading recordLen2");
        }
        header.recordLen2 = (static_cast< uint32_t >(static_cast< unsigned char >(buf[0])) << 24) |
                            (static_cast< uint32_t >(static_cast< unsigned char >(buf[1])) << 16) |
                            (static_cast< uint32_t >(static_cast< unsigned char >(buf[2])) << 8) |
                            (static_cast< uint32_t >(static_cast< unsigned char >(buf[3])));
      }

      header.spackFlag = (header.versionFlag == 0x05 || header.versionFlag == 0x09);
      if (header.recordLen1 == 0 && header.recordLen2 == 0)
      {
        throw std::runtime_error("Record length is 0");
      }
      if (header.recordLen1 != 0 && header.recordLen2 != 0 && header.recordLen1 != header.recordLen2)
      {
        throw std::runtime_error("Ambiguous record length (recordLen1 != recordLen2)");
      }
      header.recordLength = (header.recordLen1 != 0) ? header.recordLen1 : header.recordLen2;
      // if ((flags & 0x04) == 0) => must have no ratio, no blockSize, no flags
      if ((header.flags & 0x04) == 0)
      {
        if (header.flags != 0)
        {
          throw std::runtime_error("Flags specified for non-MVS must be 0");
        }
        if (header.ratio != 0)
        {
          throw std::runtime_error("Ratio specified for non-MVS must be 0");
        }
        if (header.blockSize != 0)
        {
          throw std::runtime_error("BlockSize specified for non-MVS must be 0");
        }
      }
      break;
    }
    default: {
      char msg[64];
      std::snprintf(msg, sizeof(msg), "Terse header version not recognized: 0x%02X", header.versionFlag);
      throw std::runtime_error(msg);
    }
  }

  return header;
}
