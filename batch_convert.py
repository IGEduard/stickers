#!/usr/bin/env python3
"""
Batch converter for multiple 7TV emotes
Processes a list of emote IDs or URLs
"""

import sys
from sticker_converter import StickerConverter
from pathlib import Path

def main():
    print("🎨 WhatsApp Sticker Batch Converter")
    print("=" * 60)
    
    if len(sys.argv) < 2:
        print("\nUsage:")
        print("  python batch_convert.py <emotes_file.txt>")
        print("\nFile format (one per line):")
        print("  01F6MQ33FG000FFJ97ZB8MWV52")
        print("  01F6NACCD80006SZ7ZW5FMWKWK")
        print("  https://cdn.7tv.app/emote/...")
        return
    
    emotes_file = sys.argv[1]
    
    if not Path(emotes_file).exists():
        print(f"❌ File not found: {emotes_file}")
        return
    
    converter = StickerConverter()
    
    # Read emotes list
    with open(emotes_file, 'r') as f:
        emotes = [line.strip() for line in f if line.strip() and not line.startswith('#')]
    
    print(f"\n📋 Found {len(emotes)} emotes to convert\n")
    
    success = 0
    failed = 0
    
    for i, emote in enumerate(emotes, 1):
        print(f"\n[{i}/{len(emotes)}] Processing: {emote}")
        print("-" * 60)
        
        try:
            if emote.startswith('http'):
                result = converter.convert_from_url(emote)
            else:
                result = converter.convert_7tv_emote(emote)
            
            if result:
                success += 1
            else:
                failed += 1
                
        except Exception as e:
            print(f"❌ Error: {e}")
            failed += 1
    
    print("\n" + "=" * 60)
    print(f"✅ Successfully converted: {success}")
    print(f"❌ Failed: {failed}")
    print(f"📁 Output directory: {converter.output_dir}")


if __name__ == "__main__":
    main()
