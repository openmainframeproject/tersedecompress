#ifndef TESTHELPERS_H
#define TESTHELPERS_H

#include <fstream>
#include <gtest/gtest.h>
#include <stdexcept>
#include <streambuf>
#include <string>
#include <vector>

class TestHelpers: public ::testing::Environment
{
  std::string test_data_path;
  static TestHelpers *inst;

public:
  TestHelpers(const std::string &command_line_arg) { test_data_path = command_line_arg; }
  static TestHelpers *GetInst() { return inst; }
  static void SetInst(::testing::Environment *newinst) { inst = static_cast< TestHelpers * > (newinst); }
  void DecompressAndVerify(const std::string &file, const std::string &ext, bool textMode, bool gzipMode = false);
};

#endif // TESTHELPERS_H
