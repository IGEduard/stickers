#!/usr/bin/env python3
"""
WhatsApp Sticker Converter for 7TV Emotes
Converts images and GIFs from 7TV to WhatsApp sticker format

Requirements:
- Pillow (PIL)
- requests
- imageio (for animated stickers)

Install: pip install Pillow requests imageio imageio-ffmpeg
"""

import os
import sys
import requests
import json
from PIL import Image, ImageSequence
from io import BytesIO
import imageio
from pathlib import Path

# WhatsApp Sticker Requirements
STICKER_SIZE = (512, 512)
STATIC_MAX_SIZE_KB = 100
ANIMATED_MAX_SIZE_KB = 500
MIN_FRAME_DURATION_MS = 8
MAX_TOTAL_DURATION_MS = 10000

class StickerConverter:
    def __init__(self, output_dir="stickers_output"):
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(exist_ok=True)
        
    def download_from_7tv(self, emote_id):
        """Download emote from 7TV using emote ID"""
        api_url = f"https://7tv.io/v3/emotes/{emote_id}"
        
        try:
            response = requests.get(api_url, timeout=10)
            response.raise_for_status()
            data = response.json()
            
            # Get the best quality image URL
            host = data['host']
            files = data['host']['files']
            
            # Prefer WEBP format, fallback to other formats
            image_file = None
            for file in files:
                if file['format'] == 'WEBP':
                    image_file = file
                    break
            
            if not image_file:
                image_file = files[0]  # Use first available
            
            image_url = f"https:{host['url']}/{image_file['name']}"
            is_animated = data.get('animated', False)
            emote_name = data['name']
            
            print(f"📥 Downloading: {emote_name} ({'Animated' if is_animated else 'Static'})")
            print(f"   URL: {image_url}")
            
            # Download image
            img_response = requests.get(image_url, timeout=30)
            img_response.raise_for_status()
            
            return {
                'content': img_response.content,
                'name': emote_name,
                'animated': is_animated
            }
            
        except Exception as e:
            print(f"❌ Error downloading from 7TV: {e}")
            return None
    
    def download_from_url(self, url, name=None):
        """Download image directly from URL"""
        try:
            print(f"📥 Downloading from URL: {url}")
            response = requests.get(url, timeout=30)
            response.raise_for_status()
            
            # Detect if animated
            content_type = response.headers.get('content-type', '')
            is_animated = 'gif' in content_type.lower() or 'webp' in content_type.lower()
            
            if not name:
                name = url.split('/')[-1].split('.')[0]
            
            return {
                'content': response.content,
                'name': name,
                'animated': is_animated
            }
            
        except Exception as e:
            print(f"❌ Error downloading: {e}")
            return None
    
    def process_static_sticker(self, image_content, name):
        """Convert static image to WhatsApp sticker format"""
        try:
            # Open image
            img = Image.open(BytesIO(image_content))
            
            # Convert to RGBA if needed
            if img.mode != 'RGBA':
                img = img.convert('RGBA')
            
            # Resize to 512x512 maintaining aspect ratio
            img = self.resize_with_padding(img, STICKER_SIZE)
            
            # Save as WebP
            output_path = self.output_dir / f"{name}.webp"
            
            # Try different quality settings to get under 100KB
            for quality in range(95, 50, -5):
                buffer = BytesIO()
                img.save(buffer, format='WEBP', quality=quality, method=6)
                size_kb = len(buffer.getvalue()) / 1024
                
                if size_kb <= STATIC_MAX_SIZE_KB:
                    with open(output_path, 'wb') as f:
                        f.write(buffer.getvalue())
                    print(f"✅ Static sticker created: {output_path.name} ({size_kb:.1f} KB, quality={quality})")
                    return str(output_path)
            
            # If still too large, save with lowest quality
            img.save(str(output_path), format='WEBP', quality=50, method=6)
            size_kb = output_path.stat().st_size / 1024
            print(f"⚠️  Warning: {output_path.name} is {size_kb:.1f} KB (may exceed limit)")
            return str(output_path)
            
        except Exception as e:
            print(f"❌ Error processing static sticker: {e}")
            return None
    
    def process_animated_sticker(self, image_content, name):
        """Convert animated GIF/WebP to WhatsApp sticker format"""
        try:
            # Open animated image
            img = Image.open(BytesIO(image_content))
            
            if not getattr(img, 'is_animated', False):
                print("⚠️  Image is not animated, treating as static")
                return self.process_static_sticker(image_content, name)
            
            # Extract frames
            frames = []
            durations = []
            
            for frame in ImageSequence.Iterator(img):
                # Convert frame to RGBA
                frame = frame.convert('RGBA')
                # Resize to 512x512
                frame = self.resize_with_padding(frame, STICKER_SIZE)
                frames.append(frame)
                
                # Get frame duration (in ms)
                duration = frame.info.get('duration', 100)
                # Ensure minimum frame duration
                duration = max(duration, MIN_FRAME_DURATION_MS)
                durations.append(duration)
            
            # Check total duration
            total_duration = sum(durations)
            if total_duration > MAX_TOTAL_DURATION_MS:
                # Trim frames to fit 10 second limit
                print(f"⚠️  Animation too long ({total_duration}ms), trimming to {MAX_TOTAL_DURATION_MS}ms")
                cumulative = 0
                trimmed_frames = []
                trimmed_durations = []
                
                for frame, duration in zip(frames, durations):
                    if cumulative + duration <= MAX_TOTAL_DURATION_MS:
                        trimmed_frames.append(frame)
                        trimmed_durations.append(duration)
                        cumulative += duration
                    else:
                        break
                
                frames = trimmed_frames
                durations = trimmed_durations
            
            # Save as animated WebP
            output_path = self.output_dir / f"{name}.webp"
            
            # Try different quality settings
            for quality in range(90, 40, -10):
                buffer = BytesIO()
                frames[0].save(
                    buffer,
                    format='WEBP',
                    save_all=True,
                    append_images=frames[1:],
                    duration=durations,
                    loop=0,
                    quality=quality,
                    method=6
                )
                
                size_kb = len(buffer.getvalue()) / 1024
                
                if size_kb <= ANIMATED_MAX_SIZE_KB:
                    with open(output_path, 'wb') as f:
                        f.write(buffer.getvalue())
                    print(f"✅ Animated sticker created: {output_path.name} ({size_kb:.1f} KB, {len(frames)} frames, {sum(durations)}ms, quality={quality})")
                    return str(output_path)
            
            # Save with lowest quality if still too large
            frames[0].save(
                str(output_path),
                format='WEBP',
                save_all=True,
                append_images=frames[1:],
                duration=durations,
                loop=0,
                quality=40,
                method=6
            )
            size_kb = output_path.stat().st_size / 1024
            print(f"⚠️  Warning: {output_path.name} is {size_kb:.1f} KB (exceeds {ANIMATED_MAX_SIZE_KB} KB limit)")
            return str(output_path)
            
        except Exception as e:
            print(f"❌ Error processing animated sticker: {e}")
            import traceback
            traceback.print_exc()
            return None
    
    def resize_with_padding(self, img, size):
        """Resize image to exact size with transparent padding"""
        # Calculate scaling to fit within size while maintaining aspect ratio
        img.thumbnail(size, Image.Resampling.LANCZOS)
        
        # Create new transparent image
        new_img = Image.new('RGBA', size, (0, 0, 0, 0))
        
        # Paste resized image centered
        x = (size[0] - img.size[0]) // 2
        y = (size[1] - img.size[1]) // 2
        new_img.paste(img, (x, y), img if img.mode == 'RGBA' else None)
        
        return new_img
    
    def convert_7tv_emote(self, emote_id):
        """Convert a 7TV emote to WhatsApp sticker"""
        data = self.download_from_7tv(emote_id)
        if not data:
            return None
        
        if data['animated']:
            return self.process_animated_sticker(data['content'], data['name'])
        else:
            return self.process_static_sticker(data['content'], data['name'])
    
    def convert_from_url(self, url, name=None):
        """Convert image from URL to WhatsApp sticker"""
        data = self.download_from_url(url, name)
        if not data:
            return None
        
        if data['animated']:
            return self.process_animated_sticker(data['content'], data['name'])
        else:
            return self.process_static_sticker(data['content'], data['name'])
    
    def convert_local_file(self, file_path):
        """Convert local image file to WhatsApp sticker"""
        try:
            with open(file_path, 'rb') as f:
                content = f.read()
            
            name = Path(file_path).stem
            
            # Check if animated
            img = Image.open(file_path)
            is_animated = getattr(img, 'is_animated', False)
            img.close()
            
            if is_animated:
                return self.process_animated_sticker(content, name)
            else:
                return self.process_static_sticker(content, name)
                
        except Exception as e:
            print(f"❌ Error converting local file: {e}")
            return None


def main():
    print("🎨 WhatsApp Sticker Converter for 7TV Emotes")
    print("=" * 60)
    
    converter = StickerConverter()
    
    if len(sys.argv) < 2:
        print("\nUsage:")
        print("  python sticker_converter.py <7tv_emote_id>")
        print("  python sticker_converter.py <image_url>")
        print("  python sticker_converter.py <local_file_path>")
        print("\nExamples:")
        print("  python sticker_converter.py 01F6MQ33FG000FFJ97ZB8MWV52")
        print("  python sticker_converter.py https://cdn.7tv.app/emote/...")
        print("  python sticker_converter.py my_image.gif")
        return
    
    input_arg = sys.argv[1]
    
    # Detect input type
    if input_arg.startswith('http'):
        # URL
        result = converter.convert_from_url(input_arg)
    elif os.path.exists(input_arg):
        # Local file
        result = converter.convert_local_file(input_arg)
    else:
        # Assume it's a 7TV emote ID
        result = converter.convert_7tv_emote(input_arg)
    
    if result:
        print(f"\n🎉 Success! Sticker saved to: {result}")
    else:
        print("\n❌ Failed to convert sticker")
        sys.exit(1)


if __name__ == "__main__":
    main()
