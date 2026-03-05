export interface TestReference {
  className: string;
  methodName: string;
}

export function parseProperties(content: string): Map<string, string> {
  const result = new Map<string, string>();
  for (const line of content.split("\n")) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#") || trimmed.startsWith("!"))
      continue;
    const eqIndex = findUnescapedEquals(trimmed);
    if (eqIndex < 0) continue;
    const rawKey = trimmed.substring(0, eqIndex).trim();
    const value = trimmed.substring(eqIndex + 1).trim();
    const key = unescapePropertyKey(rawKey);
    result.set(key, value);
  }
  return result;
}

function findUnescapedEquals(line: string): number {
  for (let i = 0; i < line.length; i++) {
    if (line[i] === "\\" && i + 1 < line.length) {
      i++;
      continue;
    }
    if (line[i] === "=" || line[i] === ":") return i;
  }
  return -1;
}

function unescapePropertyKey(key: string): string {
  return key.replace(/\\(.)/g, "$1");
}

export function parseTestReference(
  value: string,
): TestReference | undefined {
  const hashIndex = value.indexOf("#");
  if (hashIndex < 0) return undefined;
  return {
    className: value.substring(0, hashIndex),
    methodName: value.substring(hashIndex + 1),
  };
}
