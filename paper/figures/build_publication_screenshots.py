from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageOps


ROOT = Path(__file__).resolve().parent
BG = "#f3f5f7"
INK = "#1f3042"


def font(size, bold=False):
    candidates = [
        Path("/System/Library/Fonts/Supplemental/Arial Bold.ttf" if bold else "/System/Library/Fonts/Supplemental/Arial.ttf"),
        Path("/System/Library/Fonts/SFNS.ttf"),
    ]
    for candidate in candidates:
        if candidate.exists():
            return ImageFont.truetype(str(candidate), size=size)
    return ImageFont.load_default()


def panel(canvas, image, box, label, title):
    x, y, w, h = box
    fitted = ImageOps.contain(image, (w, h - 62), method=Image.Resampling.LANCZOS)
    px = x + (w - fitted.width) // 2
    py = y + 58
    canvas.paste(fitted, (px, py))
    draw = ImageDraw.Draw(canvas)
    draw.rounded_rectangle((x, y, x + 46, y + 40), radius=8, fill=INK)
    draw.text((x + 13, y + 6), label, font=font(24, True), fill="white")
    draw.text((x + 60, y + 5), title, font=font(26, True), fill=INK)


def build_figure2():
    top = Image.open(ROOT / "figure2_design_top_raw.png").convert("RGB")
    bottom = Image.open(ROOT / "figure2_design_bottom_raw.png").convert("RGB")


    # Preserve the application's pixels exactly; crop only window regions relevant to the paper.
    import_and_roles = top.crop((35, 75, 775, 1425))
    facets_and_dstudy = bottom.crop((35, 235, 775, 1595))
    preview = top.crop((785, 235, 2440, 1490))

    canvas = Image.new("RGB", (2600, 1380), BG)
    panel(canvas, import_and_roles, (35, 30, 720, 1310), "A", "Imported data and column roles")
    panel(canvas, facets_and_dstudy, (785, 30, 720, 1310), "B", "Facet structure and D study settings")
    panel(canvas, preview, (1535, 30, 1030, 1310), "C", "Anonymous data preview")
    canvas.save(ROOT / "figure2_design_workflow.png", quality=95)


def build_figure3():
    heat = Image.open(ROOT / "figure3_heatmap_raw.png").convert("RGB")
    surface = Image.open(ROOT / "figure3_3d_raw.png").convert("RGB")
    # Use the visualization region only, excluding transient cursor overlays on the controls.
    heat_crop = heat.crop((800, 505, 2435, 1470))
    surface_crop = surface.crop((800, 505, 2435, 1470))

    canvas = Image.new("RGB", (2500, 1080), BG)
    panel(canvas, heat_crop, (35, 30, 1200, 1010), "A", "Engine computed Phi heatmap")
    panel(canvas, surface_crop, (1265, 30, 1200, 1010), "B", "Rotatable D study surface")
    canvas.save(ROOT / "figure3_dstudy_visualizations.png", quality=95)


if __name__ == "__main__":
    build_figure2()
    build_figure3()
