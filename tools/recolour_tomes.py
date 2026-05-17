"""
Generate per-element book textures from the Fire Book source PNGs.

Each variant is an HSV-rotated copy of `fire_book.png` / `fire_book_pages.png`
so the model geometry and pixel-level shading stay identical while the
palette retunes to fit the element's theme.

Usage:
    python tools/recolour_tomes.py
"""

from __future__ import annotations

import colorsys
from dataclasses import dataclass
from pathlib import Path

from PIL import Image


REPO_ROOT  = Path(__file__).resolve().parent.parent
TEXTURE_DIR = REPO_ROOT / "src" / "main" / "resources" / "assets" / "elementalia" / "textures" / "item"


@dataclass(frozen=True)
class Recolour:
    """How to retune the Fire palette for one element.

    hue_shift  — added to the source hue (0–1 range).
    sat_mul    — multiplied into saturation; <1 desaturates.
    val_mul    — multiplied into value (lightness); <1 darkens.
    """
    hue_shift: float
    sat_mul:   float
    val_mul:   float


# Fire source palette is warm-red (~0–25° hue). Shifts below put each element
# into the right family while keeping the relative shading intact.
RECOLOURS: dict[str, Recolour] = {
    "ice":   Recolour(hue_shift=0.52, sat_mul=0.65, val_mul=1.10),   # → pale cyan/white
    "earth": Recolour(hue_shift=0.07, sat_mul=0.80, val_mul=0.70),   # → deep brown/amber
    "wind":  Recolour(hue_shift=0.55, sat_mul=0.20, val_mul=0.95),   # → desaturated grey-blue
}


def transform_pixel(rgba: tuple[int, int, int, int], rc: Recolour) -> tuple[int, int, int, int]:
    r, g, b, a = rgba
    if a == 0:
        return rgba

    h, s, v = colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)
    h = (h + rc.hue_shift) % 1.0
    s = max(0.0, min(1.0, s * rc.sat_mul))
    v = max(0.0, min(1.0, v * rc.val_mul))
    r2, g2, b2 = colorsys.hsv_to_rgb(h, s, v)

    return (round(r2 * 255), round(g2 * 255), round(b2 * 255), a)


def recolour_image(src_path: Path, dst_path: Path, rc: Recolour) -> None:
    src = Image.open(src_path).convert("RGBA")
    out = Image.new("RGBA", src.size)
    out.putdata([transform_pixel(p, rc) for p in src.getdata()])
    out.save(dst_path, format="PNG", optimize=True)
    print(f"  wrote {dst_path.relative_to(REPO_ROOT)}")


def main() -> None:
    sources = [
        ("fire_book.png",       "{element}_book.png"),
        ("fire_book_pages.png", "{element}_book_pages.png"),
    ]

    for element, rc in RECOLOURS.items():
        print(f"{element}: hue+{rc.hue_shift:+.2f}  sat×{rc.sat_mul:.2f}  val×{rc.val_mul:.2f}")
        for src_name, dst_template in sources:
            src_path = TEXTURE_DIR / src_name
            dst_path = TEXTURE_DIR / dst_template.format(element=element)
            recolour_image(src_path, dst_path, rc)


if __name__ == "__main__":
    main()
