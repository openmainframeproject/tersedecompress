# Issue #22: Correctly Construct Default Output Filename for Datasets and Binary Mode

## Overview
This contribution addresses Issue #22 in openmainframeproject/tersedecompress where the default output filename was incorrectly constructed when providing z/OS dataset names (e.g. //'my.data.set') or when operating in binary mode (-b).

## Key Updates & Fixes

### 1. Java Implementation (src/main/java/org/openmainframeproject/tersedecompress/TerseDecompress.java)
- Enabled default output filename generation for binary mode (-b), which previously required an explicit output filename or exited.
- Added path normalization to strip leading // prefixes and surrounding single quotes ('...') common in z/OS dataset specifications.
- Appends .bin extension when binary mode (-b) is active, and .txt in text mode.

### 2. C++ Implementation (cpp/src/argumentParser.cpp)
- Updated argument parser to support default output file construction in binary mode.
- Sanitized dataset inputs starting with // and enclosed in '...'.
- Updated CLI help messages and usage documentation.

## Testing & Verification
- Validated text mode decompression defaults to <dataset_base>.txt.
- Validated binary mode -b defaults to <dataset_base>.bin.
- Verified z/OS dataset paths like //'my.dataset.name' correctly yield my.dataset.name.bin / my.dataset.name.txt.

## Open Source & Foundation Compliance
- **DCO Sign-off**: All commits signed off with Signed-off-by: Ayush Mahajan <140263932+Ayush-AM@users.noreply.github.com>.
- **Conventional Commits**: Commit subject formatted as ix: correctly construct default output filename for datasets and binary mode.
- **PR Reference**: Cross-referenced issue as Fixes #22 in Pull Request #32.
