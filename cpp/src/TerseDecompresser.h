#ifndef TERSEDECOMPRESSER_H
#define TERSEDECOMPRESSER_H

#include "TerseBlockReader.h"
#include "TerseHeader.h"

#include <zlib.h>

#include <istream>
#include <memory>
#include <ostream>
#include <vector>

struct DecompresserOptions
{
  bool textMode = true;
  bool gzipMode = false;
};

class TerseDecompresser
{
public:
  static std::unique_ptr< TerseDecompresser > create(std::istream &inputStream, std::ostream &outputStream,
                                                     const DecompresserOptions &options);
  virtual ~TerseDecompresser();
  virtual void decode() = 0;

protected:
  TerseDecompresser(std::istream &in, std::ostream &out, const TerseHeader &header, const DecompresserOptions &options);
  void putChar(int x);
  void endRecord();
  void writeBytes(const char *data, size_t size);
  void close();
  const TerseHeader header_;
  const DecompresserOptions options_;
  const size_t recordLimit_;
  z_stream *zlibStream ;
  char *zlibBuffer ;
  char *zlibBufferCursor ;
  uInt zlibBufferSize;
  std::vector< char > record_; // buffer for the current record
  std::istream &inputStream_;
  std::ostream &outputStream_;
  std::unique_ptr< TerseBlockReader > blockReader_;
};

#endif // TERSEDECOMPRESSER_H
