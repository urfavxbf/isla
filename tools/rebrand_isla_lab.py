#!/usr/bin/env python3
"""Rebrand the isolated GPL-3 Isla Lab derivative without erasing upstream attribution.

Run from repository root. Idempotent; CI builds the modified worktree before committing.
"""
from __future__ import annotations
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "isla-lab"
assert (ROOT / "LICENSE").is_file(), "GPL-3.0 LICENSE missing"
assert (ROOT / "UPSTREAM_PROVENANCE.md").is_file(), "Upstream provenance missing"

# These files intentionally retain the original project's identity and license.
PROTECTED = {"LICENSE", "COPYING", "NOTICE", "UPSTREAM_PROVENANCE.md", "ISLA_ATTRIBUTION.md"}
EXTENSIONS = {
    ".kt", ".kts", ".java", ".xml", ".md", ".txt", ".json", ".yaml",
    ".yml", ".toml", ".properties", ".gradle", ".pro", ".sh", ".html",
    ".svg", ".css", ".conf", ".gitignore", ".rules", ".csv", ".feature",
}
REPLACEMENTS = (
    ("com.agupta07505.smartisland", "com.pao.isla.lab"),
    ("com/agupta07505/smartisland", "com/pao/isla/lab"),
    ("SMART_ISLAND", "ISLA"),
    ("SmartIsland", "Isla"),
    ("smartIsland", "isla"),
    ("smart_island", "isla"),
    ("smartisland", "isla"),
    ("SMART ISLAND", "ISLA"),
    ("Smart Island", "Isla"),
    ("smart island", "isla"),
    ("Smart-Island", "Isla"),
    ("smart-island", "isla"),
)
UPSTREAM_URL = "https://github.com/agupta07505/SmartIsland"
ISLA_URL = "https://github.com/urfavxbf/isla"

def apply_brand(s: str) -> str:
    for old, new in REPLACEMENTS:
        s = s.replace(old, new)
    return s

def rebrand_content(content: str, relpath: Path) -> str:
    # Preserve licensing headers exactly as the original author supplied.
    # Also preserve upstream source links in the About/Support credit screens.
    prefix, body = "", content
    if content.lstrip().startswith("/*"):
        m = re.match(r"\A(\s*/\*.*?\*/)", content, re.DOTALL)
        if m and ("Animesh Gupta" in m.group(1) or "GNU GPL" in m.group(1)):
            prefix, body = m.group(1), content[m.end():]
    elif content.lstrip().startswith("<?xml"):
        m = re.match(r"\A(<\?xml[^>]*\?>\s*<!--.*?-->)", content, re.DOTALL)
        if m and ("Animesh Gupta" in m.group(1) or "GNU GPL" in m.group(1)):
            prefix, body = m.group(1), content[m.end():]

    # Site links for updates, issue reports and Isla release information should
    # target our own repo; upstream attribution links remain in About and Support.
    credit_screen = relpath.name in {"AboutSection.kt", "SupportSection.kt"}
    if credit_screen:
        body = body.replace(UPSTREAM_URL, "__ISLA_ORIGINAL_SOURCE_LINK__")
    else:
        body = body.replace(UPSTREAM_URL, ISLA_URL)

    body = apply_brand(body)
    body = body.replace("__ISLA_ORIGINAL_SOURCE_LINK__", UPSTREAM_URL)
    if relpath.as_posix().endswith("app/src/main/res/values/strings.xml"):
        body = body.replace(
            '<string name="made_by">Isla Lab · Based on Isla by Animesh Gupta</string>',
            '<string name="made_by">Isla Lab · Based on Smart Island by Animesh Gupta (GPL-3.0)</string>',
        )
        body = re.sub(
            r'(<string name="made_by"[^>]*>).*?(</string>)',
            r'\g<1>Isla · Based on Smart Island by Animesh Gupta (GPL-3.0)\g<2>',
            body,
        )
    return prefix + body

# 1) Source and translated UI copy. Do not modify binary, generated, or license files.
changed = 0
for path in sorted(ROOT.rglob("*")):
    if not path.is_file():
        continue
    relative = path.relative_to(ROOT)
    if path.name in PROTECTED or ".git" in relative.parts or "build" in relative.parts:
        continue
    if path.suffix not in EXTENSIONS and not path.name.startswith("."):
        continue
    try:
        old = path.read_text(encoding="utf-8")
    except UnicodeError:
        continue
    new = rebrand_content(old, relative)
    if new != old:
        path.write_text(new, encoding="utf-8")
        changed += 1

# 2) Rename source files and package folders after code references are updated.
# Rename all source-class and resource identifiers consistently, not Gradle internals.
renamed = 0
all_paths = sorted(ROOT.rglob("*"), key=lambda x: len(x.parts), reverse=True)
for item in all_paths:
    if not item.exists() or item.name in PROTECTED or item.name in {".git", ".gradle"}:
        continue
    new_name = apply_brand(item.name)
    if new_name == item.name:
        continue
    target = item.with_name(new_name)
    if target.exists():
        raise RuntimeError(f"Rebranding path collision: {item} -> {target}")
    item.rename(target)
    renamed += 1

# Move package directories from com/agupta07505/isla to com/pao/isla/lab.
for base in [ROOT / "app/src/main/java", ROOT / "app/src/test/java", ROOT / "app/src/androidTest/java",
             ROOT / "baselineprofile/src/main/java", ROOT / "baselineprofile/src/test/java", ROOT / "baselineprofile/src/androidTest/java"]:
    old = base / "com/agupta07505/isla"
    target = base / "com/pao/isla/lab"
    if old.exists():
        target.parent.mkdir(parents=True, exist_ok=True)
        if target.exists():
            raise RuntimeError(f"Unexpected duplicated package tree at {target}")
        shutil.move(str(old), str(target))
        for folder in [base / "com/agupta07505", base / "com"]:
            if folder.exists() and not any(folder.iterdir()):
                folder.rmdir()
        renamed += 1

# 3) Entirely original visual brand: Android VectorDrawable, no upstream icon artwork.
res = ROOT / "app/src/main/res"
drawables = res / "drawable"
drawables.mkdir(exist_ok=True)
(drawables / "ic_isla_launcher.xml").write_text("""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <!-- Isla: dark rounded tile, luminous violet ring, minimal white I monogram. -->
    <path android:fillColor="#101019"
        android:pathData="M25,7 H83 Q101,7 101,25 V83 Q101,101 83,101 H25 Q7,101 7,83 V25 Q7,7 25,7 Z"/>
    <path android:fillColor="#9C7AFF"
        android:pathData="M25,12 H83 Q96,12 96,25 V83 Q96,96 83,96 H25 Q12,96 12,83 V25 Q12,12 25,12 Z"/>
    <path android:fillColor="#171128"
        android:pathData="M29,19 H79 Q89,19 89,29 V79 Q89,89 79,89 H29 Q19,89 19,79 V29 Q19,19 29,19 Z"/>
    <path android:fillColor="#F9F5FF"
        android:pathData="M43,31 H65 V38 H58 V70 H65 V77 H43 V70 H50 V38 H43 Z"/>
    <path android:fillColor="#B69FFF"
        android:pathData="M71,27 A5,5 0,1 0,71.01,27 Z"/>
</vector>
""", encoding="utf-8")

manifest = ROOT / "app/src/main/AndroidManifest.xml"
m = manifest.read_text()
m = m.replace('@mipmap/ic_launcher_round', '@drawable/ic_isla_launcher')
m = m.replace('@mipmap/ic_launcher', '@drawable/ic_isla_launcher')
manifest.write_text(m)

# Adaptive-icon resources use the same new custom vector. Preserve resource
# identifiers for anything referencing the original mipmap names.
for item in res.rglob("*.xml"):
    if "mipmap-anydpi" not in item.as_posix() or not item.name.startswith("ic_launcher"):
        continue
    original = item.read_text()
    updated = original.replace(
        '@mipmap/ic_launcher_foreground', '@drawable/ic_isla_launcher'
    )
    if updated != original:
        item.write_text(updated)
# Remove original upstream launcher raster artwork: the new icon is vector-based.
# Retain names needed by build references, but map any explicit R.mipmap refs
# to the new vector first.
for item in res.rglob("*"):
    if item.is_file() and item.suffix.lower() in {".png", ".webp", ".jpg", ".jpeg"} and item.stem.startswith("ic_launcher"):
        item.unlink()

# Adapt referencing Kotlin source if any code rendered the old raster resources.
for path in ROOT.rglob("*.kt"):
    s = path.read_text()
    updated = s.replace("R.mipmap.ic_launcher_foreground", "R.drawable.ic_isla_launcher").replace(
        "R.mipmap.ic_launcher_round", "R.drawable.ic_isla_launcher"
    ).replace("R.mipmap.ic_launcher", "R.drawable.ic_isla_launcher")
    if updated != s:
        path.write_text(updated)

# A permanent, accessible attribution helps comply with derivative license terms.
notice = ROOT / "ISLA_ATTRIBUTION.md"
notice.write_text("""# Isla Lab — upstream attribution

Isla Lab is a modified derivative of **Smart Island**, copyright
(C) Animesh Gupta and other contributors, from
https://github.com/agupta07505/SmartIsland

The original work and this derivative are provided under the GNU General
Public License version 3. The full license is at [LICENSE](LICENSE).
The pinned upstream commit and initial changes are recorded in
[UPSTREAM_PROVENANCE.md](UPSTREAM_PROVENANCE.md).

Isla branding and our modifications do not imply exclusive ownership of
the original source code. Preserve author notices and make the complete
corresponding source available on redistribution.
""", encoding="utf-8")

# Guard against accidental deletion of the original license and credits.
assert "GNU GENERAL PUBLIC LICENSE" in (ROOT / "LICENSE").read_text()
assert "Animesh Gupta" in (ROOT / "UPSTREAM_PROVENANCE.md").read_text()
assert "applicationId = \"com.pao.isla.lab\"" in (ROOT / "app/build.gradle.kts").read_text()
assert (ROOT / "app/src/main/res/drawable/ic_isla_launcher.xml").exists()
print(f"Isla rebrand applied: {changed} text files updated; {renamed} paths renamed.")
print("Original legal copyright, GPL-3.0 LICENSE and provenance preserved.")
