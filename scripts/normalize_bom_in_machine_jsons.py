from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MACHINES_DIR = ROOT / "app" / "src" / "main" / "assets" / "machines"
TARGETS = ["karakuri.json", "lupin.json", "tokyoghoul.json", "vvv.json"]

for name in TARGETS:
    path = MACHINES_DIR / name
    text = path.read_text(encoding="utf-8-sig")
    path.write_text(text, encoding="utf-8")
    print(f"normalized BOM: {name}")
