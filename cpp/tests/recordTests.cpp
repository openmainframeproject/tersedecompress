// Copyright Contributors to the TerseDecompress Project.
// SPDX-License-Identifier: Apache-2.0

#include "../src/Constants.h"
#include "../src/TerseDecompresser.h"

#include <gtest/gtest.h>
#include <sstream>
#include <vector>

namespace {
// Append 12-bit codes to a header, followed by the end marker (0).
std::string encode(std::string data, const std::vector< unsigned > &codes)
{
  unsigned bits = 0;
  int available = 0;
  auto code = [&](unsigned value) {
    bits = (bits << 12) | value;
    available += 12;
    while (available >= 8)
    {
      available -= 8;
      data.push_back(static_cast< char >(bits >> available));
    }
  };
  for (auto value : codes) code(value);
  code(0);
  if (available != 0) data.push_back(static_cast< char >(bits << (8 - available)));
  return data;
}

std::string variableRecords(std::initializer_list< size_t > lengths)
{
  std::vector< unsigned > codes;
  for (auto length : lengths)
  {
    codes.insert(codes.end(), length, 1);
    codes.push_back(257);
  }
  return encode(std::string("\x05\x01\x7f\xff\0\0\0\0\0\0\0\0", 12), codes);
}
}

TEST(RecordTests, PdsPackHeader)
{
  // https://public.dhe.ibm.com/s390/mvs/tools/IMBDSHIP.JCL.TRSD
  std::istringstream input(std::string("\x07\0\0\x50\x0c\x03\x6d\x10\0\0\0\0", 12));
  const auto header = TerseHeader::parseHeader(input);
  EXPECT_FALSE(header.spackFlag);
  EXPECT_FALSE(header.variableFlag);
  EXPECT_EQ(header.recordLength, 80U);
  EXPECT_EQ(header.blockSize, 27920U);
}

TEST(RecordTests, PdsSpackHeader)
{
  // https://public.dhe.ibm.com/s390/cbpacs/fixes/R17.SCPPLOAD.TRS
  std::istringstream input(std::string("\x09\x01\x18\0\x8c\x02\x18\0\0\0\0\0", 12));
  const auto header = TerseHeader::parseHeader(input);
  EXPECT_TRUE(header.spackFlag);
  EXPECT_TRUE(header.variableFlag);
  EXPECT_EQ(header.recordLength, 6144U);
  EXPECT_EQ(header.blockSize, 6144U);
}

TEST(RecordTests, NativeModePreservesBinaryBytes)
{
  for (bool textMode : {false, true})
  {
    std::istringstream input(std::string("\x01\x89\x69\xa5\0\x01\x0c\x20\0", 9));
    std::ostringstream output;
    DecompresserOptions options;
    options.textMode = textMode;
    auto decoder = TerseDecompresser::create(input, output, options);
    decoder->decode();
    EXPECT_EQ(output.str(), std::string("\xc1", 1));
  }
}

TEST(RecordTests, NativeModeIgnoresHeaderRecordLength)
{
  // Native mode has no host records, so a zero or short record length in the header must not matter.
  const size_t length = Tersedecompress::MAXRECORD + 10;
  for (const char *recordLength : {"\0\0", "\0\x01"})
  {
    std::string header("\x01\x89\x69\xa5", 4);
    header.append(recordLength, 2);
    std::istringstream input(encode(header, std::vector< unsigned >(length, 0xc2)));
    std::ostringstream output;
    auto decoder = TerseDecompresser::create(input, output, DecompresserOptions{});
    decoder->decode();
    EXPECT_EQ(output.str(), std::string(length, '\xc1'));
  }
}

TEST(RecordTests, HugeFixedRecordLengthDoesNotPreallocate)
{
  // SPACK, fixed, recordLen2 = 0x7fffffff: allocating that up front would need 2 GB.
  std::istringstream input(encode(std::string("\x05\0\0\0\0\0\0\0\x7f\xff\xff\xff", 12), {0xc2}));
  std::ostringstream output;
  DecompresserOptions options;
  options.textMode = false;
  auto decoder = TerseDecompresser::create(input, output, options);
  decoder->decode();
  EXPECT_EQ(output.str(), std::string("\xc1", 1));
}

TEST(RecordTests, FullVariableTextRecordsHaveRoomForNewlines)
{
  std::istringstream input(variableRecords({32767, 32767}));
  std::ostringstream output;
  auto decoder = TerseDecompresser::create(input, output, DecompresserOptions{});
  decoder->decode();
  std::string expected(2 * 32768 + 1, '\0');
  expected[32767] = expected[65535] = expected[65536] = '\n';
  EXPECT_EQ(output.str(), expected);
}

TEST(RecordTests, RecordLimitDoesNotGrowAfterFullRecord)
{
  std::istringstream input(variableRecords({32767, 32768}));
  std::ostringstream output;
  auto decoder = TerseDecompresser::create(input, output, DecompresserOptions{});
  EXPECT_THROW(decoder->decode(), std::runtime_error);
}

TEST(RecordTests, FixedRecordMarksDoNotBecomeBytes)
{
  // SPACK header, record mark (257), literal (1), and end marker (0).
  const char data[] = {5, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 16, 16, 1, 0, 0};
  for (bool textMode : {false, true})
  {
    std::istringstream input(std::string(data, sizeof(data)));
    std::ostringstream output;
    DecompresserOptions options;
    options.textMode = textMode;
    auto decoder = TerseDecompresser::create(input, output, options);
    decoder->decode();
    EXPECT_EQ(output.str(), textMode ? std::string("\0\n", 2) : std::string("\0", 1));
  }
}
