#!/usr/bin/env bash
set -euo pipefail

# This script iterates over all *.json files in the current folder.
# For every object with "_type":"DV_CODED_TEXT" that is missing "value",
# it copies defining_code.code_string into "value".
#
# Requires: jq

shopt -s nullglob

for f in ./*.json; do
  echo "Processing: $f"

  jq '
    def fix_dv_coded_text:
      if (type == "object")
         and (._type? == "DV_CODED_TEXT")
         and (has("value") | not)
         and (.defining_code? | type == "object")
         and (.defining_code.code_string? != null)
      then
        . + { "value": .defining_code.code_string }
      else
        .
      end;

    walk(fix_dv_coded_text)
  ' "$f" > "${f}.tmp" && mv "${f}.tmp" "$f"

done

echo "Done."
