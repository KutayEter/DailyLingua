import json

def update_json_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    words = data['words']
    for word in words:
        # Örnek cümle çevirisini ekle
        word['exampleTranslation'] = f"'{word['translation']}' kelimesini basit bir cümlede kullanıyorum."
        
        # Seçenekleri Türkçe yap
        new_options = []
        for option in word['options']:
            # Eğer seçenek doğru cevapsa, kelimenin çevirisini kullan
            if option == word['correct']:
                new_options.append(word['translation'])
            else:
                # Diğer seçenekler için rastgele Türkçe kelimeler
                new_options.append(option)  # Bu kısmı düzenleyeceğiz
        
        word['options'] = new_options
        # Doğru cevabı Türkçe yap
        word['correct'] = word['translation']
    
    # Güncellenen JSON'u kaydet
    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

# Tüm dil dosyalarını güncelle
files = [
    'app/src/main/assets/words_en.json',
    'app/src/main/assets/words_de.json',
    'app/src/main/assets/words_ru.json'
]

for file in files:
    update_json_file(file)