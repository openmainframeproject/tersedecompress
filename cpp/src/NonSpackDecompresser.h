#ifndef NONSPACKDECOMPRESSER_H
#define NONSPACKDECOMPRESSER_H

#include "TerseBlockReader.h"
#include "TerseDecompresser.h"
#include "TerseHeader.h"
#include <memory>

class NonSpackDecompresser: public TerseDecompresser
{
public:
  NonSpackDecompresser(std::istream &in, std::ostream &out, const TerseHeader &header, const DecompresserOptions &options);
  virtual ~NonSpackDecompresser() = default;
  void decode() override;
};

#endif // NONSPACKDECOMPRESSER_H
