import os
import re
import sys

# Ensure UTF-8 printing
try:
    sys.stdout.reconfigure(encoding='utf-8')
except Exception:
    pass

prepare_dir = r"e:\AI Interview coach\app\src\main\assets\prepare"
categories = ["logical", "quantitative", "verbal"]

headings = set()

for cat in categories:
    cat_dir = os.path.join(prepare_dir, cat)
    if not os.path.isdir(cat_dir):
        continue
    for file in os.listdir(cat_dir):
        if file.endswith(".md"):
            filepath = os.path.join(cat_dir, file)
            with open(filepath, "r", encoding="utf-8") as f:
                for line in f:
                    if line.startswith("#"):
                        m = re.match(r"^(#+)\s*(.*)", line)
                        if m:
                            level = len(m.group(1))
                            title = m.group(2).strip()
                            headings.add((level, title))

print(f"Total unique headings: {len(headings)}")

# Group headings containing certain keywords
keywords = ["solved", "practice", "questions", "concept check", "summary", "takeaways", "congratulations", "revision", "formula", "mistakes", "tcs", "infosys", "accenture", "capgemini", "cognizant", "deloitte", "wipro"]
keyword_matches = {kw: [] for kw in keywords}

for h in headings:
    title_lower = h[1].lower()
    for kw in keywords:
        if kw in title_lower:
            keyword_matches[kw].append(f"L{h[0]}: {h[1]}")

for kw in keywords:
    print(f"\nKeyword '{kw}' ({len(keyword_matches[kw])} matches):")
    # Sort and take unique matches to make clean output
    unique_matches = sorted(list(set(keyword_matches[kw])))
    for m in unique_matches[:10]:
        print(f"  {m}")
