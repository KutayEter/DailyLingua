#!/usr/bin/env python3
"""
Temizleme aracı: app/src/main/assets içindeki words_*.json dosyalarında
"sentence_123" gibi yer tutucu veya açıkça hatalı 'word' alanlarını kaldırır.

Kullanım: workspace kökünden çalıştırın:
  python scripts/clean_assets.py

Yapılanlar:
 - Orijinal dosyanın bir kopyasını <file>.bak-clean'e kaydeder.
 - Filtrelenen dosyayı aynı yola yazar.
 - Konsola temizlenen dosya ve silinen kayıt sayısını yazdırır.
"""
import json
import re
from pathlib import Path

ASSETS_DIR = Path("app/src/main/assets")

def should_remove(word: str) -> bool:
    if not word:
        return True
    # Yer tutucu pattern: sentence_123 veya sentence123 gibi
    if re.match(r"^sentence_\d+$", word):
        return True
    if word.startswith("sentence_"):
        return True
    # Diğer bariz hatalı tokenlar
    if word.strip().lower() == "old (object)":
        # Bu tekil bir örnek; silmek yerine bırakmak daha güvenli.
        return False
    return False

def clean_file(path: Path):
    text = path.read_text(encoding="utf-8")
    try:
        data = json.loads(text)
    except Exception as e:
        print(f"{path}: JSON parse error: {e}")
        return

    if not isinstance(data, dict) or "words" not in data:
        print(f"{path}: beklenen format değil (top-level 'words' yok). Atlanıyor.")
        return

    words = data["words"]
    if not isinstance(words, list):
        print(f"{path}: 'words' liste değil. Atlanıyor.")
        return

    removed = 0
    kept = []
    for w in words:
        word_text = w.get("word") if isinstance(w, dict) else None
        if word_text is None:
            # beklenmeyen yapı, koru
            kept.append(w)
            continue
        if should_remove(word_text):
            removed += 1
        else:
            kept.append(w)

    if removed == 0:
        print(f"{path.name}: temizlenecek kayıt yok (0).")
        return

    # Yedekle
    bak = path.with_suffix(path.suffix + ".bak-clean")
    path.replace(bak)
    # Yeni veri yaz
    new_data = {"words": kept}
    path.write_text(json.dumps(new_data, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"{path.name}: temizlendi. Silinen kayıt sayısı: {removed}. Yedek: {bak.name}")

def main():
    if not ASSETS_DIR.exists():
        print("assets klasörü bulunamadı:", ASSETS_DIR)
        return

    for p in ASSETS_DIR.glob("words_*.json"):
        clean_file(p)

if __name__ == "__main__":
    main()
