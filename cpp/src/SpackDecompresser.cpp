#include "SpackDecompresser.h"
#include "Constants.h"

#include <stdexcept>

SpackDecompresser::SpackDecompresser(std::istream &in, std::ostream &out, const TerseHeader &header,
                                     const DecompresserOptions &options)
    : TerseDecompresser(in, out, header, options)
{
  tree_.resize(Tersedecompress::TREESIZE + 1);
}

void SpackDecompresser::treeInit()
{
  for (auto &node : tree_)
  {
    node.Left = NONE;
    node.Right = NONE;
    node.Back = 0;
    node.NextCount = 0;
  }
  for (int init_index = BASE; init_index <= CODESIZE; ++init_index)
  {
    tree_[init_index].Left = NONE;
    tree_[init_index].Right = init_index;
  }
  for (int init_index = CODESIZE + 1; init_index <= (Tersedecompress::TREESIZE - 1); ++init_index)
  {
    tree_[init_index].NextCount = init_index + 1;
    tree_[init_index].Left = NONE;
    tree_[init_index].Right = NONE;
  }

  tree_[Tersedecompress::TREESIZE].NextCount = NONE;
  tree_[BASE].NextCount = BASE;
  tree_[BASE].Back = BASE;
  for (int i = 1; i <= CODESIZE; ++i)
  {
    tree_[i].NextCount = NONE;
  }
  treeAvail_ = CODESIZE + 1;
}

int SpackDecompresser::getTreeNode()
{
  node_ = treeAvail_;
  treeAvail_ = tree_[node_].NextCount;
  return node_;
}

void SpackDecompresser::putChars(int X)
{
  stack_.Head = 0;
  while (true)
  {
    while (X > CODESIZE)
    {
      if (stack_.Head >= STACKSIZE)
      {
        throw std::runtime_error("SPACK decode error: tree too deep, file corrupted?");
      }
      stack_.Head++;
      stack_.Data[stack_.Head] = tree_[X].Right;
      X = tree_[X].Left;
    }
    if (X < 0)
    {
      throw std::runtime_error("SPACK decode error: got negative code, file corrupted?");
    }
    putChar(X);
    if (stack_.Head > 0)
    {
      X = stack_.Data[stack_.Head];
      stack_.Head--;
    }
    else
    {
      break;
    }
  }
}

void SpackDecompresser::bumpRef(int bref)
{
  if (tree_[bref].NextCount < 0)
  {
    tree_[bref].NextCount--;
  }
  else
  {
    forwards_ = tree_[bref].NextCount;
    prev_ = tree_[bref].Back;
    tree_[prev_].NextCount = forwards_;
    tree_[forwards_].Back = prev_;
    tree_[bref].NextCount = -1;
  }
}

void SpackDecompresser::lruKill()
{
  lru_p_ = tree_[0].NextCount;
  lru_q_ = tree_[lru_p_].NextCount;
  lru_r_ = tree_[lru_p_].Back;
  tree_[lru_q_].Back = lru_r_;
  tree_[lru_r_].NextCount = lru_q_;
  deleteRef(tree_[lru_p_].Left);
  deleteRef(tree_[lru_p_].Right);
  tree_[lru_p_].NextCount = treeAvail_;
  treeAvail_ = lru_p_;
}

void SpackDecompresser::deleteRef(int dref)
{
  if (dref < 0 || dref >= static_cast< int >(tree_.size()))
  {
    if (dref == NONE)
      return;
    throw std::runtime_error("SPACK: invalid ref in deleteRef()");
  }

  if (tree_[dref].NextCount == -1)
  {
    lruAdd(dref);
  }
  else
  {
    tree_[dref].NextCount++;
  }
}

void SpackDecompresser::lruAdd(int lruNext)
{
  lru_back_ = tree_[BASE].Back;
  tree_[lruNext].NextCount = BASE;
  tree_[BASE].Back = lruNext;
  tree_[lruNext].Back = lru_back_;
  tree_[lru_back_].NextCount = lruNext;
}

void SpackDecompresser::decode()
{
  treeInit();
  tree_[Tersedecompress::TREESIZE - 1].NextCount = NONE;

  int H = blockReader_->getBlok();
  if (H == Tersedecompress::ENDOFFILE)
  {
    close();
    return;
  }
  putChars(H);
  int G = blockReader_->getBlok();
  while (G != Tersedecompress::ENDOFFILE)
  {
    if (treeAvail_ == NONE)
    {
      lruKill();
    }
    putChars(G);
    int N = getTreeNode();
    tree_[N].Left = H;
    tree_[N].Right = G;
    bumpRef(H);
    bumpRef(G);
    lruAdd(N);
    H = G;
    G = blockReader_->getBlok();
  }
  close();
}
