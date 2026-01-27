#!/usr/bin/env python3
import json
import re
from pathlib import Path
from typing import Any, Dict, List, Set, Union

Json = Union[Dict[str, Any], List[Any], str, int, float, bool, None]

RE_RES_REF = re.compile(r"^[A-Z][A-Za-z]+/([A-Za-z0-9\-.]{1,128})$")
RE_URN = re.compile(r"^urn:(?:uuid|oid):([A-Za-z0-9\-.]{1,128})$")
RE_URL_TAIL = re.compile(r"^https?://.*/([A-Za-z0-9\-.]{1,128})$")
RE_CONTAINED = re.compile(r"^#([A-Za-z0-9\-.]{1,128})$")


def iter_strings(x: Json):
    if isinstance(x, str):
        yield x
    elif isinstance(x, dict):
        for v in x.values():
            yield from iter_strings(v)
    elif isinstance(x, list):
        for v in x:
            yield from iter_strings(v)


def collect_referenced_ids(doc: Json) -> Set[str]:
    keep: Set[str] = set()
    for s in iter_strings(doc):
        for rx in (RE_RES_REF, RE_URN, RE_URL_TAIL, RE_CONTAINED):
            m = rx.match(s)
            if m:
                keep.add(m.group(1))
                break
    return keep


def clean_node(node: Json) -> Json:
    """Remove meta.security, request, and fullUrl keys anywhere; remove empty meta."""
    if isinstance(node, dict):
        out: Dict[str, Any] = {}
        for k, v in node.items():
            if k in ("request", "fullUrl"):
                continue
            if k == "meta" and isinstance(v, dict):
                meta = {mk: clean_node(mv) for mk, mv in v.items() if mk != "security"}
                if meta:
                    out["meta"] = meta
                continue
            out[k] = clean_node(v)
        return out
    if isinstance(node, list):
        return [clean_node(v) for v in node]
    return node


def drop_unreferenced_ids(node: Json, keep: Set[str], *, is_root: bool) -> Json:
    """Drop id fields that are not referenced; always drop root id."""
    if isinstance(node, dict):
        out: Dict[str, Any] = {}
        for k, v in node.items():
            if k == "id":
                if is_root:
                    continue
                if isinstance(v, str) and v in keep:
                    out[k] = v
                continue
            out[k] = drop_unreferenced_ids(v, keep, is_root=False)
        return out
    if isinstance(node, list):
        return [drop_unreferenced_ids(v, keep, is_root=False) for v in node]
    return node


def clean_doc(doc: Json) -> Json:
    if not isinstance(doc, dict):
        return doc

    keep = collect_referenced_ids(doc)

    out = dict(doc)
    out.pop("id", None)
    out.pop("timestamp", None)
    if "type" in out:
        out["type"] = "collection"

    out = clean_node(out)
    out = drop_unreferenced_ids(out, keep, is_root=True)
    return out


def main() -> None:
    files = sorted(Path(".").glob("*.json"))
    if not files:
        print("No *.json files found in the current folder.")
        return

    for path in files:
        try:
            doc = json.loads(path.read_text(encoding="utf-8"))
        except Exception as e:
            print(f"SKIP (invalid json): {path.name} ({e})")
            continue

        cleaned = clean_doc(doc)
        path.write_text(json.dumps(cleaned, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        print(f"CLEANED: {path.name}")


if __name__ == "__main__":
    main()
