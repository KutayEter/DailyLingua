#!/usr/bin/env python3
import json
import re
import sys
from pathlib import Path

def merge_file(path: Path):
    text = path.read_text(encoding='utf-8')
    parts = re.split(r"\}\s*\n\{", text)
    objects = []
    for i, part in enumerate(parts):
        if i == 0 and len(parts) == 1:
            candidate = part
        elif i == 0:
            candidate = part + '\n}'
        elif i == len(parts) - 1:
            candidate = '{\n' + part
        else:
            candidate = '{\n' + part + '\n}'
        try:
            obj = json.loads(candidate)
            objects.append(obj)
        except json.JSONDecodeError as e:
            print(f"Warning: part {i+1} could not be parsed: {e}")

    combined = []
    seen_ids = set()
    duplicates = []
    blocks_parsed = 0
    for obj in objects:
        if 'words' in obj and isinstance(obj['words'], list):
            blocks_parsed += 1
            for w in obj['words']:
                wid = w.get('id')
                if wid in seen_ids:
                    duplicates.append(wid)
                    continue
                seen_ids.add(wid)
                combined.append(w)

    backup = path.with_suffix(path.suffix + '.bak')
    backup.write_text(text, encoding='utf-8')

    out = {'words': combined}
    path.write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding='utf-8')

    print(f"Parsed blocks: {blocks_parsed}")
    print(f"Total words combined: {len(combined)}")
    print(f"Duplicate ids skipped: {len(duplicates)}")
    if duplicates:
        print("Some duplicate ids (kept first occurrence):", duplicates[:50])
    print(f"Backup of original saved to: {backup}")
    print(f"Merged file written to: {path}")

if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('Usage: merge_asset_file.py <path-to-asset-json>')
        sys.exit(2)
    target = Path(sys.argv[1]).resolve()
    if not target.exists():
        print('Target does not exist:', target)
        sys.exit(1)
    merge_file(target)
