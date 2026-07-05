#!/usr/bin/env python3
"""
Audit UI:
- Replace magic .dp (1,2,4,6,8,10,12,14,16,20,24) with Spacing constants.
- Generate contrast ratio report for Light/Dark color schemes.
"""

import re
from pathlib import Path

# Mapping from number string to constant name
DP_MAP = {
    "1": "Spacing1",
    "2": "Spacing2",
    "4": "Spacing4", 
    "6": "Spacing6",
    "8": "Spacing8",
    "10": "Spacing10",
    "12": "Spacing12",
    "14": "Spacing14",
    "16": "Spacing16",
    "20": "Spacing20",
    "24": "Spacing24"
}

DP_PATTERN = re.compile(r'\b(1|2|4|6|8|10|12|14|16|20|24)\.dp\b')

def replace_dp(match):
    num = match.group(1)
    return DP_MAP[num]

def process_kt_files(base_dir):
    base_path = Path(base_dir)
    if not base_path.exists():
        print(f"Directory not found: {base_path}")
        return []
    changed_files = []
    for kt_file in base_path.rglob('*.kt'):
        if kt_file.name == 'Theme.kt':
            continue
        content = kt_file.read_text(encoding='utf-8')
        if DP_PATTERN.search(content):
            new_content = DP_PATTERN.sub(replace_dp, content)
            if new_content != content:
                kt_file.write_text(new_content, encoding='utf-8')
                changed_files.append(str(kt_file))
    return changed_files

def hex_to_rgb(hex_str):
    hex_str = hex_str.strip('#')
    if len(hex_str) == 6:
        r = int(hex_str[0:2], 16)
        g = int(hex_str[2:4], 16)
        b = int(hex_str[4:6], 16)
        return r, g, b
    return None

def relative_luminance(r, g, b):
    def adjust(c):
        c = c / 255.0
        return c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4
    return 0.2126 * adjust(r) + 0.7152 * adjust(g) + 0.0722 * adjust(b)

def contrast_ratio(lum1, lum2):
    lighter = max(lum1, lum2)
    darker = min(lum1, lum2)
    return (lighter + 0.05) / (darker + 0.05)

def parse_color_scheme(theme_text, scheme_name):
    color_type = 'light' if scheme_name == 'Light' else 'dark'
    pattern = re.compile(
        r'val Senior' + scheme_name + r'ColorScheme = ' + color_type + r'ColorScheme\((.*?)\)',
        re.DOTALL
    )
    match = pattern.search(theme_text)
    if not match:
        return {}
    body = match.group(1)
    scheme = {}
    color_constants = {
        'DeepRed': '#FFC62828',
        'JadeGreen': '#FF2E7D32',
        'Gold': '#FFF9A825',
        'SoftBg': '#FFF7F8FA',
        'CardWhite': '#FFFFFFFF',
        'DarkText': '#FF1A1A1A',
        'GrayText': '#FF666666',
        'LightRedBg': '#FFFFEBEE',
        'LightGreenBg': '#FFE8F5E9',
        'LightGoldBg': '#FFFFF8E1',
        'LightGrayDivider': '#FFEEEEEE',
        'BrownText': '#FF5D4037'
    }
    if scheme_name == 'Dark':
        color_constants.update({
            'SoftBg': '#FF121212',
            'GrayText': '#FFB3B3B3'
        })
    for line in body.splitlines():
        line = line.strip()
        if not line or line.startswith('//'):
            continue
        m = re.match(r'(\w+)\s*=\s*([\w\.]+|Color\(0x[0-9A-Fa-f]+\))', line)
        if m:
            key = m.group(1)
            val = m.group(2)
            if val in color_constants:
                scheme[key] = color_constants[val]
            elif val.startswith('Color(0x'):
                hex_raw = val.split('0x')[1]
                if len(hex_raw) >= 6:
                    hex6 = hex_raw[-6:].upper()
                    scheme[key] = '#' + hex6
    return scheme

def audit_contrast(theme_path):
    with open(theme_path, 'r', encoding='utf-8') as f:
        text = f.read()
    light_scheme = parse_color_scheme(text, 'Light')
    dark_scheme = parse_color_scheme(text, 'Dark')
    combos = [
        ('onPrimary', 'primary'),
        ('onSecondary', 'secondary'),
        ('onTertiary', 'tertiary'),
        ('onBackground', 'background'),
        ('onSurface', 'surface'),
        ('onSurfaceVariant', 'surfaceVariant'),
        ('onError', 'error')
    ]
    def print_contrast(scheme_name, scheme):
        print(f"\n{scheme_name} contrast ratios:")
        for on, base in combos:
            on_hex = scheme.get(on)
            base_hex = scheme.get(base)
            if not on_hex or not base_hex:
                print(f"  {on}/{base}: missing colors (on={on_hex}, base={base_hex})")
                continue
            rgb_on = hex_to_rgb(on_hex)
            rgb_base = hex_to_rgb(base_hex)
            if not rgb_on or not rgb_base:
                print(f"  {on}/{base}: invalid hex")
                continue
            lum_on = relative_luminance(*rgb_on)
            lum_base = relative_luminance(*rgb_base)
            ratio = contrast_ratio(lum_on, lum_base)
            status = "PASS" if ratio >= 4.5 else "WARN"
            print(f"  {on}/{base}: {ratio:.2f}:1 [{status}]")
    print_contrast('Light', light_scheme)
    print_contrast('Dark', dark_scheme)

def main():
    kt_dir = Path('app/src/main/java/com/licham')
    print("=== Replace magic .dp → Spacing constants ===")
    changed = process_kt_files(kt_dir)
    if changed:
        for f in changed:
            print(f"Updated: {f}")
    else:
        print("No .dp changes.")
    print("\n=== Contrast Audit ===")
    theme_file = kt_dir / 'Theme.kt'
    if theme_file.exists():
        audit_contrast(theme_file)
    else:
        print(f"Theme file not found: {theme_file}")

if __name__ == '__main__':
    main()