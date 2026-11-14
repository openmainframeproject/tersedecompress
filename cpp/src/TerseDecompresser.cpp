#include "TerseDecompresser.h"
#include "Constants.h"
#include "NonSpackDecompresser.h"
#include "SpackDecompresser.h"

#include <algorithm>
#include <cstring>
#include <iostream>
#include <stdexcept>

std::unique_ptr< TerseDecompresser > TerseDecompresser::create(std::istream &inputStream, std::ostream &outputStream,
                                                               const DecompresserOptions &options)
{
  TerseHeader header = TerseHeader::parseHeader(inputStream);
  if (header.spackFlag)
  {
    return std::make_unique< SpackDecompresser >(inputStream, outputStream, header, options);
  }
  else
  {
    return std::make_unique< NonSpackDecompresser >(inputStream, outputStream, header, options);
  }
}

TerseDecompresser::~TerseDecompresser()
{
  if (zlibBuffer != nullptr)
  {
    delete[] zlibBuffer;
    zlibBuffer = nullptr;
  }
  if (zlibStream != nullptr)
  {
    deflateEnd(zlibStream);
    delete zlibStream;
    zlibStream = nullptr;
  }
}

TerseDecompresser::TerseDecompresser(std::istream &in, std::ostream &out, const TerseHeader &header, const DecompresserOptions &options)
    // Native mode files have no host records: never convert them, and write them out in chunks.
    : header_(header), options_{options.textMode && header.versionFlag != 0x01, options.gzipMode},
      recordLimit_(header.variableFlag || header.versionFlag == 0x01 ? Tersedecompress::MAXRECORD
                                                                     : header.recordLength),
      zlibStream(nullptr), zlibBuffer(nullptr),
      zlibBufferCursor(nullptr), zlibBufferSize(128 * 1024), inputStream_(in), outputStream_(out)
{
  blockReader_ = std::make_unique< TerseBlockReader >(inputStream_);
  // Reserve the record buffer up front, but don't trust a huge record length from the header.
  record_.reserve(std::min(recordLimit_, Tersedecompress::MAXRECORD));
  if (options_.gzipMode)
  {
    auto buffer = std::make_unique< char[] >(zlibBufferSize);
    auto stream = std::make_unique< z_stream >();
    // 15 | 16 = 31 for gzip header and trailer
    if (deflateInit2(stream.get(), Z_DEFAULT_COMPRESSION, Z_DEFLATED, 15 | 16, 8, Z_DEFAULT_STRATEGY) != Z_OK)
    {
      throw std::runtime_error("Unable to initialize gzip compression");
    }
    zlibBuffer = buffer.release();
    zlibBufferCursor = zlibBuffer;
    zlibStream = stream.release();
  }
}

void TerseDecompresser::putChar(int x)
{
  if (x == 0 && options_.textMode && header_.variableFlag == true)
  {
      endRecord();
  }
  else if (x == Tersedecompress::RECORDMARK)
  {
    if (header_.variableFlag)
    {
      endRecord();
    }
  }
  else
  {
    if (x < 1 || x > 256)
    {
      throw std::runtime_error("Invalid literal in decompressed record");
    }
    if (record_.size() >= recordLimit_)
    {
      throw std::runtime_error("Record size exceeds expected maximum. File may be corrupted or not a valid Terse file.");
    }
    record_.push_back(options_.textMode ? static_cast< char >(Tersedecompress::EbcToAsc[x - 1])
                                        : static_cast< char >(x - 1));
    if (header_.variableFlag != true && record_.size() == recordLimit_)
    {
      endRecord();
    }
  }
}

void TerseDecompresser::endRecord()
{
  if (header_.variableFlag && !options_.textMode)
  {
    // Write a 4-byte Record Descriptor Word (RDW)
    // RDW = (record_.size() + 4) in high-order 2 bytes + low-order 2 bytes
    size_t lengthWithRDW = record_.size() + 4;
    // For MVS VB:
    //   bytes[0..1] = (16-bit) length of record
    //   bytes[2..3] = reserved (usually zero)
    unsigned char rdw[4];
    rdw[0] = static_cast< unsigned char >((lengthWithRDW >> 8) & 0xFF); // high-order length byte
    rdw[1] = static_cast< unsigned char >(lengthWithRDW & 0xFF);        // low-order length byte
    rdw[2] = 0x00;
    rdw[3] = 0x00;
    writeBytes(reinterpret_cast< const char * >(rdw), 4);
  }
  writeBytes(record_.data(), record_.size());
  if (options_.textMode)
  {
    writeBytes("\n", 1);
  }
  record_.clear();
}

void TerseDecompresser::writeBytes(const char *data, size_t size)
{
  if (size == 0)
  {
    return;
  }
  if (zlibStream != nullptr)
  {
    zlibStream->avail_in = static_cast< uInt >(size);
    zlibStream->next_in = reinterpret_cast< Bytef * >(const_cast< char * >(data));
    do
    {
      zlibStream->avail_out = static_cast< uInt >(zlibBufferSize - (zlibBufferCursor - zlibBuffer));
      zlibStream->next_out = reinterpret_cast< Bytef * >(zlibBufferCursor);
      if (deflate(zlibStream, Z_NO_FLUSH) != Z_OK)
      {
        throw std::runtime_error("Gzip compression failed");
      }
      if (zlibStream->avail_out == 0)
      {
        outputStream_.write(zlibBuffer, zlibBufferSize);
        zlibBufferCursor = zlibBuffer;
      }
      else
      {
        zlibBufferCursor += (zlibBufferSize - (zlibBufferCursor - zlibBuffer)) - zlibStream->avail_out;
      }
    } while (zlibStream->avail_in != 0);
  }
  else
  {
    outputStream_.write(data, size);
  }
  if (!outputStream_)
  {
    throw std::runtime_error("Unable to write decompressed output");
  }
}

void TerseDecompresser::close() 
{
  if (!record_.empty() || (options_.textMode && header_.variableFlag))
  {
    endRecord();
  }
  if (zlibStream != nullptr)
  {
    zlibStream->avail_in = 0;
    zlibStream->next_in = nullptr;
    int result;
    do
    {
      uInt bufferAvail = static_cast< uInt >(zlibBufferSize - (zlibBufferCursor - zlibBuffer));
      zlibStream->avail_out = bufferAvail;
      zlibStream->next_out = reinterpret_cast< Bytef * >(zlibBufferCursor);
      result = deflate(zlibStream, Z_FINISH);
      if (result != Z_OK && result != Z_STREAM_END)
      {
        throw std::runtime_error("Unable to finish gzip compression");
      }
      if (zlibStream->avail_out == 0)
      {
        outputStream_.write(zlibBuffer, zlibBufferSize);
        zlibBufferCursor = zlibBuffer;
      }
      else
      {
        zlibBufferCursor += bufferAvail - zlibStream->avail_out;
      }
    } while (result != Z_STREAM_END);
    if (zlibBufferCursor != zlibBuffer)
    {
      outputStream_.write(zlibBuffer, zlibBufferCursor - zlibBuffer);
    }
  }
  outputStream_.flush();
  if (!outputStream_)
  {
    throw std::runtime_error("Unable to flush decompressed output");
  }
}
