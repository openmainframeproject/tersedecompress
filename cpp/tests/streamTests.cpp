// Copyright Contributors to the TerseDecompress Project.
// SPDX-License-Identifier: Apache-2.0

#include "../src/TerseBlockReader.h"
#include <gtest/gtest.h>
#include <sstream>
#include <stdexcept>

TEST(StreamTests, ReadFailureIsNotEndOfFile)
{
  for (bool leftoverBits : {false, true})
  {
    std::istringstream input(std::string("\0\0", 2));
    TerseBlockReader reader(input);
    if (leftoverBits) reader.getBlok();
    input.setstate(std::ios::badbit);
    EXPECT_THROW(reader.getBlok(), std::runtime_error);
  }
}

TEST(StreamTests, NormalEndOfFileIsAccepted)
{
  std::istringstream input;
  TerseBlockReader reader(input);
  EXPECT_EQ(reader.getBlok(), 0);
}
