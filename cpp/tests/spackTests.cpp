// Copyright Contributors to the TerseDecompress Project.
// SPDX-License-Identifier: Apache-2.0

// Unit tests for the SPACK decoder's guards against corrupt input. They set
// up the decoder's tree directly instead of decoding a file.

#include "../src/SpackDecompresser.h"

#include <gtest/gtest.h>
#include <sstream>
#include <stdexcept>

struct SpackDecompresserTestAccess
{
  static constexpr int STACKSIZE = SpackDecompresser::STACKSIZE;
  static void treeInit(SpackDecompresser &d) { d.treeInit(); }
  static void putChars(SpackDecompresser &d, int x) { d.putChars(x); }
  static void addNode(SpackDecompresser &d, int h, int g) { d.addNode(h, g); }
  static int nextFreeNode(SpackDecompresser &d) { return d.treeAvail_; }
  // Make node `node` a chain of `depth` left links ending in literal 1,
  // with literal 1 as every right child.
  static void makeLeftChain(SpackDecompresser &d, int node, int depth)
  {
    for (int i = 0; i < depth; ++i)
    {
      d.tree_[node + i].Left = i == 0 ? 1 : node + i - 1;
      d.tree_[node + i].Right = 1;
    }
  }
};

namespace {
using Access = SpackDecompresserTestAccess;
constexpr int FIRST_NODE = 258; // first node after the 257 literal codes

struct SpackFixture
{
  std::istringstream input;
  std::ostringstream output;
  TerseHeader header;
  SpackDecompresser decoder;

  static TerseHeader makeHeader()
  {
    TerseHeader header;
    header.versionFlag = 0x05;
    header.spackFlag = true;
    header.variableFlag = true;
    return header;
  }

  SpackFixture() : header(makeHeader()), decoder(input, output, header, binary())
  {
    Access::treeInit(decoder);
  }

  static DecompresserOptions binary()
  {
    DecompresserOptions options;
    options.textMode = false;
    return options;
  }
};
}

TEST(SpackTests, DeepTreeWithinStackLimitDecodes)
{
  SpackFixture f;
  const int depth = Access::STACKSIZE;
  Access::makeLeftChain(f.decoder, FIRST_NODE, depth);
  EXPECT_NO_THROW(Access::putChars(f.decoder, FIRST_NODE + depth - 1));
}

TEST(SpackTests, TreeDeeperThanStackIsRejected)
{
  SpackFixture f;
  const int depth = Access::STACKSIZE + 1;
  Access::makeLeftChain(f.decoder, FIRST_NODE, depth);
  EXPECT_THROW(Access::putChars(f.decoder, FIRST_NODE + depth - 1), std::runtime_error);
}

TEST(SpackTests, NodeCanLinkOtherCodes)
{
  SpackFixture f;
  EXPECT_NO_THROW(Access::addNode(f.decoder, 1, 2));
}

TEST(SpackTests, ReusedNodeAsRightChildIsRejected)
{
  SpackFixture f;
  const int next = Access::nextFreeNode(f.decoder);
  EXPECT_THROW(Access::addNode(f.decoder, 1, next), std::runtime_error);
}

TEST(SpackTests, ReusedNodeAsLeftChildIsRejected)
{
  SpackFixture f;
  const int next = Access::nextFreeNode(f.decoder);
  EXPECT_THROW(Access::addNode(f.decoder, next, 1), std::runtime_error);
}
