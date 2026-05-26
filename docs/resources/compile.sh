#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

LUALATEX="${LUALATEX:-}"
if [[ -z "$LUALATEX" ]]; then
  if [[ -x "/Library/TeX/texbin/lualatex" ]]; then
    LUALATEX="/Library/TeX/texbin/lualatex"
  else
    LUALATEX="$(command -v lualatex || true)"
  fi
fi

if [[ -z "$LUALATEX" || ! -x "$LUALATEX" ]]; then
  echo "Error: lualatex is not installed or not found in PATH." >&2
  echo "Install a TeX distribution (MacTeX on macOS, TeX Live on Linux, MiKTeX on Windows)." >&2
  exit 1
fi

export TEXINPUTS="./tex//:./img//:${TEXINPUTS:-}"
export OPENTYPEFONTS="./fonts//:${OPENTYPEFONTS:-}"
export TTFONTS="./fonts//:${TTFONTS:-}"

TEXFILE="rapport_v1.tex"

"$LUALATEX" -interaction=nonstopmode -file-line-error "$TEXFILE"
"$LUALATEX" -interaction=nonstopmode -file-line-error "$TEXFILE"

echo
echo "PDF generated: rapport_v1.pdf"
