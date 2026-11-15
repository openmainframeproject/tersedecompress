#include "argumentParser.h"

#include <iostream>
#include <string>
#include <unordered_map>
#include <vector>

#ifndef GIT_TAG
#define GIT_TAG "unknown"
#endif

ArgumentParser::ArgumentParser(int argc, char **argv) { parseArguments(argc, argv); }

bool ArgumentParser::hasFlag(const std::string &flag) const { return flags.find(flag) != flags.end(); }

std::string ArgumentParser::getValue(const std::string &key) const
{
  auto it = values.find(key);
  return (it != values.end()) ? it->second : "";
}

void ArgumentParser::showHelp() const
{
  std::cout << "Usage: tersedecompress++ [options] <input_file> [output_file]\n";
  std::cout << "Options:\n";
  std::cout << "  -h           Show this help message.\n";
  std::cout << "  -b           Enable binary mode (no EBCDIC->ASCII conversion).\n";
  std::cout << "  -z           Enable gzip compression for the output file.\n";
  std::cout << "Arguments:\n";
  std::cout << "  input_file   Path to the input file (required).\n";
  std::cout << "  output_file  Path to the output file (optional).\n";
  std::cout << "               In text mode, output_file defaults to <input file>.txt.\n";
  std::cout << "Version: 5.1, commit " << GIT_TAG << "\n";
}

std::string ArgumentParser::getInputFile() const { return inputFile; }

std::string ArgumentParser::getOutputFile() const { return outputFile; }

void ArgumentParser::parseArguments(int argc, char **argv)
{
  std::vector< std::string > args(argv + 1, argv + argc);

  for (size_t i = 0; i < args.size(); ++i)
  {
    if (args[i] == "-h")
    {
      flags["-h"] = true;
    }
    else if (args[i] == "-b")
    {
      flags["-b"] = true;
    }
    else if (args[i] == "-z")
    {
      flags["-z"] = true;
    }
    else if (inputFile.empty())
    {
      inputFile = args[i];
    }
    else if (outputFile.empty())
    {
      outputFile = args[i];
    }
    else
    {
      std::cerr << "Unknown argument: " << args[i] << "\n";
      showHelp();
      std::exit(1);
    }
  }

  if (inputFile.empty() && !hasFlag("-h"))
  {
    std::cerr << "Error: input_file is required.\n";
    showHelp();
    std::exit(1);
  }
  if (outputFile.empty() != true)
  {
    return;
  }
  // A z/OS data set name such as //'HLQ.DATA' can't be turned into a valid output name.
  if (!hasFlag("-h") && inputFile.rfind("//", 0) == 0)
  {
    std::cerr << "Error: output_file is required for a data set input.\n";
    std::exit(1);
  }
  std::string outputFileExtension = hasFlag("-b") ? ".bin" : ".txt";
  outputFileExtension += hasFlag("-z") ? ".gz" : "";
  outputFile = inputFile + outputFileExtension;
}