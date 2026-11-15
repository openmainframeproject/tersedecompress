// Copyright Contributors to the TerseDecompress Project.
// SPDX-License-Identifier: Apache-2.0

#include "testHelpers.h"
#include "../src/TerseDecompresser.h"

#include <sstream>

TEST(GzipTests, CorpusRoundTrip)
{
  for (const auto *format : {"PACK", "SPACK"})
    for (const auto *file : {"FB.RANDOM.TXT", "VB.RANDOM.TXT", "FB.BIBLE.TXT"})
      for (bool textMode : {false, true})
      {
        SCOPED_TRACE(std::string(file) + "." + format + (textMode ? " text" : " binary"));
        TestHelpers::GetInst()->DecompressAndVerify(file, format, textMode, true);
      }
}

TEST(GzipTests, EmptySpackStillProducesCompleteGzipStream)
{
  const char header[] = {5, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0};
  std::istringstream input(std::string(header, sizeof(header)));
  std::ostringstream output;
  DecompresserOptions options;
  options.textMode = false;
  options.gzipMode = true;
  auto decoder = TerseDecompresser::create(input, output, options);
  decoder->decode();
  std::string compressed = output.str();
  char inflated[1];
  z_stream stream{};
  ASSERT_EQ(inflateInit2(&stream, 15 + 16), Z_OK);
  stream.next_in = reinterpret_cast< Bytef * >(compressed.data());
  stream.avail_in = static_cast< uInt >(compressed.size());
  stream.next_out = reinterpret_cast< Bytef * >(inflated);
  stream.avail_out = sizeof(inflated);
  const int result = inflate(&stream, Z_FINISH);
  const auto size = stream.total_out;
  inflateEnd(&stream);
  EXPECT_EQ(result, Z_STREAM_END);
  EXPECT_EQ(size, 0UL);
}

TEST(GzipTests, FailedOutputIsReported)
{
  // PACK header, one literal byte (code 1), then an end marker (code 0).
  const char data[] = {2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 0};
  for (bool gzipMode : {false, true})
  {
    std::istringstream input(std::string(data, sizeof(data)));
    std::ostringstream output;
    output.setstate(std::ios::badbit);
    DecompresserOptions options;
    options.textMode = false;
    options.gzipMode = gzipMode;
    auto decoder = TerseDecompresser::create(input, output, options);
    EXPECT_THROW(decoder->decode(), std::runtime_error);
  }
}
