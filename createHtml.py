from pathlib import Path
import json
from string import Template

ROOT = Path(".")

EXCLUDED_DIRS = {
    "build", "generated", ".gradle",
    "intermediates", "cxx",
    "test", "androidTest", ".idea"
}

EXCLUDED_XML_FILES = {
    "ic_launcher.xml",
    "ic_launcher_round.xml",
    "ic_launcher_background.xml",
    "ic_launcher_foreground.xml",
    "backup_rules.xml",
    "data_extraction_rules.xml",
    "device_streaming.xml",
    "deviceStreaming.xml",
    "misc.xml",
    "runConfigurations.xml",
    "migrations.xml",
    "compiler.xml",
    "workspace.xml",
    "gradle.xml",
    "vcs.xml"
}


def is_bad_path(path: Path) -> bool:
    return any(part in EXCLUDED_DIRS for part in path.parts)


def collect_files():
    items = []

    for file in ROOT.rglob("*"):
        if file.is_dir():
            continue
        if is_bad_path(file):
            continue

        ext = file.suffix.lower()
        if ext not in {".java", ".xml"}:
            continue

        if ext == ".xml" and file.name in EXCLUDED_XML_FILES:
            continue

        try:
            content = file.read_text(encoding="utf-8", errors="ignore")
        except:
            continue

        items.append({
            "path": str(file),
            "content": content
        })

    return items


def build_html(data):
    json_data = json.dumps(data)

    html_template = Template(r"""
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
<title>Code Explorer</title>

<style>
body {
    font-family: Arial;
    display: flex;
    margin: 0;
}

#sidebar {
    width: 320px;
    border-right: 1px solid #ccc;
    height: 100vh;
    overflow: auto;
    padding: 10px;
}
#content {
    flex: 1;
    padding: 10px;
    overflow: hidden;   /* important: stop double scroll */
    display: flex;      /* make it a flex column */
    flex-direction: column;
    min-height: 0;
}

pre {
    background: #111;
    color: #0f0;
    padding: 10px;

    flex: 1;            /* take remaining space */
    min-height: 0;
    overflow: auto;     /* single scroll container */
}

input {
    width: 100%;
    padding: 6px;
    margin-bottom: 10px;
}

.folder {
    cursor: pointer;
    font-weight: bold;
    user-select: none;
    margin-top: 4px;
}

.folder::before {
    content: "📁 ";
}

.folder.open::before {
    content: "📂 ";
}

.file {
    cursor: pointer;
    padding-left: 18px;
    user-select: none;
}

.file:hover {
    background: #eee;
}

.hidden {
    display: none;
}
</style>
</head>

<body>

<div id="sidebar">
<input id="search" placeholder="Search..." oninput="filterTree()" />
<div id="tree"></div>
</div>

<div id="content">
<pre id="viewer">Click a file</pre>
</div>

<script>
const DATA = $data;

function insertPath(tree, parts, file) {
    let node = tree;

    for (let i = 0; i < parts.length; i++) {
        const part = parts[i];

        if (i === parts.length - 1) {
            if (!node.files) node.files = [];
            node.files.push(file);
        } else {
            if (!node.dirs) node.dirs = {};
            if (!node.dirs[part]) node.dirs[part] = {};
            node = node.dirs[part];
        }
    }
}

function buildTree() {
    const tree = {};

    for (const f of DATA) {
        const parts = f.path.split(/[/\\]+/);
        insertPath(tree, parts, f);
    }

    const root = document.getElementById("tree");
    root.innerHTML = "";
    root.appendChild(renderNode("root", tree));
}

function compressPath(name, node) {
    let path = name;
    let current = node;

    while (
        current.dirs &&
        Object.keys(current.dirs).length === 1 &&
        (!current.files || current.files.length === 0)
    ) {
        const nextKey = Object.keys(current.dirs)[0];
        path += "." + nextKey;
        current = current.dirs[nextKey];
    }

    return { path: path, node: current };
}
function renderNode(name, node, depth = 0) {
    const container = document.createElement("div");

    const compressed = compressPath(name, node);
    const path = compressed.path;
    const actualNode = compressed.node;

    const folder = document.createElement("div");
    folder.className = "folder";
    folder.textContent = path;

    // 👇 indentation based on depth
    folder.style.paddingLeft = (depth * 14) + "px";

    const children = document.createElement("div");
    children.className = "hidden";

    folder.onclick = () => {
        children.classList.toggle("hidden");
        folder.classList.toggle("open");
    };

    if (actualNode.dirs) {
        for (const key of Object.keys(actualNode.dirs).sort()) {
            children.appendChild(
                renderNode(key, actualNode.dirs[key], depth + 1)
            );
        }
    }

    if (actualNode.files) {
        for (const f of actualNode.files) {
            const fileDiv = document.createElement("div");
            fileDiv.className = "file";
            fileDiv.textContent = f.path.split(/[/\\]+/).pop();
            fileDiv.title = f.path;

            fileDiv.style.paddingLeft = (depth * 14 + 18) + "px";

            fileDiv.onclick = () => {
                document.getElementById("viewer").textContent = f.content;
            };

            children.appendChild(fileDiv);
        }
    }

    container.appendChild(folder);
    container.appendChild(children);

    return container;
}

function filterTree() {
    const q = document.getElementById("search").value.toLowerCase();

    document.querySelectorAll(".file").forEach(el => {
        el.style.display = el.textContent.toLowerCase().includes(q)
            ? "block"
            : "none";
    });
}

buildTree();
</script>

</body>
</html>
""")

    return html_template.substitute(data=json_data)


def main():
    data = collect_files()

    with open("output.html", "w", encoding="utf-8") as f:
        f.write(build_html(data))

    print("Generated output.html")


main()
