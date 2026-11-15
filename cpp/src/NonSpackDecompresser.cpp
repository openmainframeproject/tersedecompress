#include "NonSpackDecompresser.h"
#include "Constants.h"

#include <stdexcept>
#include <vector>

NonSpackDecompresser::NonSpackDecompresser(std::istream &in, std::ostream &out, const TerseHeader &header,
                                           const DecompresserOptions &options)
    : TerseDecompresser(in, out, header, options)
{
}

void NonSpackDecompresser::decode()
{
  std::vector< int > Father(Tersedecompress::TREESIZE, 0);
  std::vector< int > CharExt(Tersedecompress::TREESIZE, 0);
  std::vector< int > Backward(Tersedecompress::TREESIZE, 0);
  std::vector< int > Forward(Tersedecompress::TREESIZE, 0);

  int H1 = 0, H2 = 0;
  int x = 0, d = 0, y = 0;
  int q = 0, r = 0, e = 0, p = 0, h = 0;

  H2 = 65;

  for (H1 = 258; H1 < Tersedecompress::TREESIZE; ++H1)
  {
    Father[H1] = H2;
    CharExt[H1] = 65;
    H2 = H1;
  }

  for (H1 = 258; H1 < (Tersedecompress::TREESIZE - 1); ++H1)
  {
    Backward[H1 + 1] = H1;
    Forward[H1] = H1 + 1;
  }

  Backward[0] = Tersedecompress::TREESIZE - 1;
  Forward[0] = 258;
  Backward[258] = 0;
  Forward[Tersedecompress::TREESIZE - 1] = 0;

  x = 0;
  d = blockReader_->getBlok();

  while (d != Tersedecompress::ENDOFFILE)
  {
    h = 0;
    y = Backward[0];
    q = Backward[y];
    Backward[0] = q;
    Forward[q] = 0;
    h = y;
    p = 0;

    while (d > 257)
    {
      q = Forward[d];
      r = Backward[d];
      Forward[r] = q;
      Backward[q] = r;
      Forward[d] = h;
      Backward[h] = d;
      h = d;
      e = Father[d];
      Father[d] = p;
      p = d;
      d = e;
    }

    q = Forward[0];
    Forward[y] = q;
    Backward[q] = y;
    Forward[0] = h;
    Backward[h] = 0;

    CharExt[x] = d;
    putChar(d);

    x = y;

    while (p != 0)
    {
      e = Father[p];
      putChar(CharExt[p]);
      Father[p] = d;
      d = p;
      p = e;
    }

    Father[y] = d;
    d = blockReader_->getBlok();
  }
  close();
}
