import json
import re
from pathlib import Path

root = Path(__file__).resolve().parents[1]
assets = root / 'app' / 'src' / 'main' / 'assets'
input_path = assets / 'words_en.json'
backup_path = assets / 'words_en.json.bak'

text = input_path.read_text(encoding='utf-8')
# Split into top-level JSON objects by looking for a closing brace followed by optional whitespace and an opening brace.
parts = re.split(r"\}\s*\n\{", text)
objects = []
for i, part in enumerate(parts):
    # Rebuild full JSON text for each part
    if i == 0 and len(parts) == 1:
        candidate = part
    elif i == 0:
        candidate = part + '\n}'
    elif i == len(parts) - 1:
        candidate = '{\n' + part
    else:
        candidate = '{\n' + part + '\n}'
    # Try to parse
    try:
        obj = json.loads(candidate)
        objects.append(obj)
    except json.JSONDecodeError as e:
        # Try a more permissive attempt: wrap entire file as array
        # We'll skip unparseable parts but report them
        print(f"Warning: part {i+1} could not be parsed: {e}")

# Gather all words lists
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

# Backup original
backup_path.write_text(text, encoding='utf-8')

# Write merged JSON
out = {'words': combined}
input_path.write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding='utf-8')

# Print summary
print(f"Parsed blocks: {blocks_parsed}")
print(f"Total words combined: {len(combined)}")
print(f"Duplicate ids skipped: {len(duplicates)}")
if duplicates:
    print("Some duplicate ids (kept first occurrence):", duplicates[:50])
print(f"Backup of original saved to: {backup_path}")
print(f"Merged file written to: {input_path}")
