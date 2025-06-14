# app/utils/parser.py
import re, yaml
from pathlib import Path

STORY_DIR = Path("story")

def parse_markdown_to_scene(md_text: str) -> dict:
    """
    --- 헤더 YAML 분리 & 본문 텍스트+choices 추출
    """
    match = re.match(r"---\n(.*?)\n---\n(.*)", md_text, re.DOTALL)
    if not match:
        raise ValueError("Invalid markdown format")
    yaml_text, content = match.groups()
    meta = yaml.safe_load(yaml_text)

    lines = content.strip().split("\n")
    text_lines, choices = [], []
    for line in lines:
        line = line.strip()
        if line.startswith("- "):
            parts = re.split(r"→|->", line[2:], maxsplit=1)
            if len(parts) == 2:
                choices.append({"text": parts[0].strip(), "next": parts[1].strip()})
        elif line:
            text_lines.append(line)

    return {
        **meta,
        "text": " ".join(text_lines),
        "choices": choices
    }
