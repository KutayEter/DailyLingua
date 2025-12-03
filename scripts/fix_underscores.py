#!/usr/bin/env python3
"""
Fix underscores in word fields inside assets JSON files.

This script:
 - Looks for files matching app/src/main/assets/words_*.json
 - For each entry in data['words'], if the 'word' value contains '_' it replaces
   underscores with spaces, except for tokens that look like placeholders
   (e.g. sentence_123) or all-upper identifiers.
 - Writes a backup to <file>.bak-underscores and overwrites the file.
 - Prints a per-file summary of replacements.

Run from repo root:
  python scripts/fix_underscores.py
"""
import json
import re
from pathlib import Path

ROOT = Path(".")
ASSETS_GLOB = Path("app/src/main/assets")

placeholder_re = re.compile(r"^sentence_\d+$", re.IGNORECASE)

def should_fix_word(s: str) -> bool:
    if not s or "_" not in s:
        return False
    # skip placeholders like sentence_123
    if placeholder_re.match(s):
        return False
    # skip things that look like identifiers with no letters
    if not re.search(r"[A-Za-zÇĞIİÖŞÜçğıiöşüА-Яа-я]", s):
        return False
    # otherwise we will replace underscores with spaces
    return True

def process_file(p: Path):
    text = p.read_text(encoding="utf-8")
    try:
        data = json.loads(text)
    except Exception as e:
        print(f"{p.name}: JSON parse error: {e}")
        return
    if not isinstance(data, dict) or "words" not in data:
        print(f"{p.name}: top-level 'words' not found, skipping")
        return
    words = data["words"]
    changed = 0
    for w in words:
        if isinstance(w, dict) and "word" in w:
            orig = w["word"]
            if should_fix_word(orig):
                new = orig.replace("_", " ")
                if new != orig:
                    w["word"] = new
                    changed += 1
    if changed > 0:
        bak = p.with_suffix(p.suffix + ".bak-underscores")
        p.replace(bak)
        p.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"{p.name}: replaced underscores in {changed} entries; backup: {bak.name}")
    else:
        print(f"{p.name}: no changes needed")

def main():
    if not ASSETS_GLOB.exists():
        print("Assets folder not found:", ASSETS_GLOB)
        return
    files = list(ASSETS_GLOB.glob("words_*.json"))
    if not files:
        print("No words_*.json files found")
        return
    for f in files:
        process_file(f)

if __name__ == '__main__':
    main()
