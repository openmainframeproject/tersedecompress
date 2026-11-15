#ifndef TERSEHEADER_H
#define TERSEHEADER_H

#include <cstdint>
#include <istream>

class TerseHeader
{
public:
  uint8_t versionFlag = 0;
  bool variableFlag = false;
  uint16_t recordLen1 = 0;
  uint8_t flags = 0;
  uint8_t ratio = 0;
  uint16_t blockSize = 0;
  uint32_t recordLen2 = 0;

  size_t recordLength = 0; // final chosen record length (recordLen1 or recordLen2)
  bool spackFlag = true;
  static TerseHeader parseHeader(std::istream &in);
};

#endif // TERSEHEADER_H
