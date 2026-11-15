#ifndef SPACKDECOMPRESSER_H
#define SPACKDECOMPRESSER_H

#include "TerseBlockReader.h"
#include "TerseDecompresser.h"
#include "TerseHeader.h"
#include <memory>

class SpackDecompresser: public TerseDecompresser
{
public:
  SpackDecompresser(std::istream &in, std::ostream &out, const TerseHeader &header, const DecompresserOptions &options);
  virtual ~SpackDecompresser() = default;
  void decode() override;

private:
  friend struct SpackDecompresserTestAccess;
  void putChars(int x);
  void treeInit();
  int getTreeNode();
  void addNode(int H, int G);
  void bumpRef(int bref);
  void lruKill();
  void deleteRef(int dref);
  void lruAdd(int lruNext);

private:
  struct TreeRecord
  {
    int Left = 0;
    int Right = 0;
    int Back = 0;
    int NextCount = 0;
  };
  static constexpr int NONE = -1;
  static constexpr int BASE = 0;
  static constexpr int CODESIZE = 257; // 2^8 + 1
  std::vector< TreeRecord > tree_;
  static constexpr int STACKSIZE = 0x07FF;
  struct StackType
  {
    int Head = 0;
    int Data[STACKSIZE + 1]; // +1 so we can index up to [STACKSIZE]
  } stack_;

  int treeAvail_ = 0;
  int node_ = 0;
  int forwards_ = 0;
  int prev_ = 0;
  int lru_p_ = 0;
  int lru_q_ = 0;
  int lru_r_ = 0;
  int lru_back_ = 0;
};

#endif // SPACKDECOMPRESSER_H
