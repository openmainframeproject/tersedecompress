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
  std::cout << "Arguments:\n";
  std::cout << "  input_file   Path to the input file (required).\n";
  std::cout << "  output_file  Path to the output file (optional).\n";
  std::cout << "               If not provided, defaults to <input file>.txt (or .bin in binary mode).\n";
  std::cout << "Version: 5.0.1, commit " << GIT_TAG << "\n";
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
  
  if (outputFile.empty())
  {
    std::string baseName = inputFile;
    if (baseName.find("//") == 0) {
      baseName = baseName.substr(2);
    }
    if (baseName.length() >= 2 && baseName.front() == '\'' && baseName.back() == '\'') {
      baseName = baseName.substr(1, baseName.length() - 2);
    }
    outputFile = baseName + (hasFlag("-b") ? ".bin" : ".txt");
  }
}