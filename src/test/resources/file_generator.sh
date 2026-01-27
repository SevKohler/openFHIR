#!/usr/bin/env bash
set -euo pipefail

URL="http://localhost:8080/openfhir/toopenehr"
BASE_DIR="${1:-kds/fall/toOpenEHR}"

INPUT_DIR="$BASE_DIR/input"
OUTPUT_DIR="$BASE_DIR/output"

tmp_resp="$(mktemp)"
trap 'rm -f "$tmp_resp"' EXIT

if [[ ! -d "$INPUT_DIR" ]]; then
  echo "❌ Input directory not found: $INPUT_DIR"
  exit 1
fi

mkdir -p "$OUTPUT_DIR"

find "$INPUT_DIR" -type f -name "*.json" ! -name "Composition-*.json" -print0 \
| while IFS= read -r -d '' f; do
    base="$(basename "$f")"
    out_file="$OUTPUT_DIR/Composition-${base#*-}"

    echo "POST: $f"

    code="$(
      curl -sS \
        -o "$tmp_resp" \
        -w '%{http_code}' \
        -H 'Content-Type: application/json' \
        --data-binary "@$f" \
        "$URL" \
        || echo "000"
    )"

    if [[ "$code" == "200" ]]; then
      mv "$tmp_resp" "$out_file"
      echo "  ✔ wrote $out_file"
    else
      echo "  ✘ skipped (HTTP $code)"
    fi
done
