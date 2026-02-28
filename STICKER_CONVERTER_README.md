# 🎨 WhatsApp Sticker Converter for 7TV Emotes

Convertor automat pentru transformarea emote-urilor de pe 7TV în format WhatsApp Sticker.

## 📋 Cerințe

### Instalare Python packages:
```bash
pip install Pillow requests imageio imageio-ffmpeg
```

## 🚀 Utilizare

### 1. Convertește un singur emote de pe 7TV

**Folosind Emote ID:**
```bash
python sticker_converter.py 01F6MQ33FG000FFJ97ZB8MWV52
```

**Folosind URL direct:**
```bash
python sticker_converter.py https://cdn.7tv.app/emote/01F6MQ33FG000FFJ97ZB8MWV52/4x.webp
```

**Folosind fișier local:**
```bash
python sticker_converter.py my_emote.gif
```

### 2. Convertește multiple emote-uri (Batch)

Creează un fișier text cu lista de emote IDs:

**emotes_list.txt:**
```
# Animated emotes
01F6MQ33FG000FFJ97ZB8MWV52
60ae8f5d259ac5a73e56a426
60ae39e7259ac5a73e4da1d6

# Static emotes
01F6NACCD80006SZ7ZW5FMWKWK
60ae434b77137b000de9e675
```

Apoi rulează:
```bash
python batch_convert.py emotes_list.txt
```

## 📖 Cum să găsești Emote ID de pe 7TV

### Metodă 1: Din URL
URL-ul emote-ului arată așa:
```
https://7tv.app/emotes/01F6MQ33FG000FFJ97ZB8MWV52
                         ↑
                    Acesta este ID-ul
```

### Metodă 2: Cu Developer Tools
1. Deschide pagina emote-ului pe 7TV
2. Apasă F12 (Developer Tools)
3. Du-te la tab-ul Network
4. Reîmprospătează pagina (F5)
5. Caută request-ul care conține emote data
6. Găsești ID-ul în JSON response

### Metodă 3: Click dreapta pe imagine
1. Click dreapta pe imaginea emote-ului
2. "Copy image address"
3. URL-ul va conține ID-ul

## 📐 Specificații WhatsApp Sticker

### Static Stickers:
- ✅ Format: WebP
- ✅ Dimensiune: 512x512 pixeli
- ✅ Fundal: Transparent
- ✅ Max size: 100 KB

### Animated Stickers:
- ✅ Format: WebP animat
- ✅ Dimensiune: 512x512 pixeli
- ✅ Fundal: Transparent
- ✅ Max size: 500 KB
- ✅ Min frame duration: 8ms
- ✅ Max total duration: 10 secunde

## 🎯 Features

### ✨ Procesare automată:
- 📥 Download automat de pe 7TV
- 🔄 Redimensionare la 512x512 cu padding transparent
- 🎨 Conversie la format WebP
- 📊 Optimizare automată pentru a respecta limitele de dimensiune
- ⏱️ Ajustare frame duration pentru animated stickers
- ✂️ Trimming automat dacă animația depășește 10 secunde

### 🛡️ Validări:
- ✅ Verificare dimensiune fișier
- ✅ Verificare durată animație
- ✅ Verificare frame duration
- ✅ Optimizare calitate automată

## 📂 Output

Toate stickerele convertite se salvează în directorul `stickers_output/`:

```
stickers_output/
├── emote_name_1.webp
├── emote_name_2.webp
└── emote_name_3.webp
```

## 🔧 Opțiuni Avansate

### Schimbă directorul de output:
```python
from sticker_converter import StickerConverter

converter = StickerConverter(output_dir="my_stickers")
converter.convert_7tv_emote("01F6MQ33FG000FFJ97ZB8MWV52")
```

### Folosește ca librărie:
```python
from sticker_converter import StickerConverter

converter = StickerConverter()

# Convert from 7TV
converter.convert_7tv_emote("EMOTE_ID")

# Convert from URL
converter.convert_from_url("https://example.com/image.gif", name="my_sticker")

# Convert local file
converter.convert_local_file("/path/to/image.png")
```

## ⚠️ Troubleshooting

### "❌ Error downloading from 7TV"
- Verifică că emote ID-ul este corect
- Verifică conexiunea la internet
- Emote-ul ar putea fi șters de pe 7TV

### "⚠️ Warning: file exceeds limit"
- Scriptul încearcă automat să optimizeze
- Pentru animated: reduce numărul de frame-uri manual
- Pentru static: reduce dimensiunea imaginii înainte de conversie

### "Module not found"
```bash
pip install Pillow requests imageio imageio-ffmpeg
```

## 📝 Exemple Complete

### Exemplu 1: Convertește un emote animat
```bash
# Emote animat de pe 7TV
python sticker_converter.py 01F6MQ33FG000FFJ97ZB8MWV52

# Output:
# 📥 Downloading: PepePls (Animated)
# ✅ Animated sticker created: PepePls.webp (487.3 KB, 60 frames, 3000ms, quality=80)
```

### Exemplu 2: Convertește un emote static
```bash
python sticker_converter.py 01F6NACCD80006SZ7ZW5FMWKWK

# Output:
# 📥 Downloading: Pepega (Static)
# ✅ Static sticker created: Pepega.webp (45.2 KB, quality=90)
```

### Exemplu 3: Batch conversion
```bash
# Creează emotes_list.txt cu ID-uri
python batch_convert.py emotes_list.txt

# Output:
# 📋 Found 10 emotes to convert
# [1/10] Processing: 01F6MQ33FG000FFJ97ZB8MWV52
# ✅ Successfully converted: 9
# ❌ Failed: 1
```

## 🎨 Integrare cu Android App

După conversie, copiază fișierele WebP în:
```
Android/app/src/main/assets/[pack_id]/
```

Și actualizează `contents.json`:
```json
{
  "image_file": "PepePls.webp",
  "emojis": ["😂", "🎉"],
  "accessibility_text": "A green frog character dancing happily"
}
```

## 🔗 Link-uri Utile

- 7TV: https://7tv.app/
- WhatsApp Stickers Guide: https://faq.whatsapp.com/general/26000226
- WebP Tools: https://developers.google.com/speed/webp

## 📄 License

MIT License - Free to use and modify
