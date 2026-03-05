export interface ApproveChain {
  approveOffset: number;
  chainEndOffset: number;
  terminalMethod: string | null;
}

const TERMINAL_METHODS = new Set(["by", "byFile", "byValue"]);

export function hasApproveJImport(text: string): boolean {
  return /\bimport\s+(?:static\s+)?org\.approvej\./.test(text);
}

export function findApproveChains(text: string): ApproveChain[] {
  if (!hasApproveJImport(text)) return [];

  const results: ApproveChain[] = [];
  const approveRegex = /\bapprove\s*\(/g;
  let match;

  while ((match = approveRegex.exec(text)) !== null) {
    const approveOffset = match.index;
    const openParenIndex = approveOffset + match[0].length - 1;

    const afterArgs = skipBalancedParens(text, openParenIndex);
    if (afterArgs < 0) continue;

    let pos = afterArgs;
    let lastMethod = "approve";
    let chainEnd = afterArgs;

    while (pos < text.length) {
      pos = skipWhitespaceAndComments(text, pos);
      if (pos >= text.length || text[pos] !== ".") break;
      pos++;
      pos = skipWhitespaceAndComments(text, pos);

      const methodStart = pos;
      while (pos < text.length && /[a-zA-Z0-9_]/.test(text[pos])) pos++;
      const methodName = text.substring(methodStart, pos);

      if (methodName.length === 0) break;

      pos = skipWhitespaceAndComments(text, pos);
      if (pos >= text.length) break;

      if (text[pos] === "(") {
        const afterMethodArgs = skipBalancedParens(text, pos);
        if (afterMethodArgs < 0) break;
        pos = afterMethodArgs;
      } else if (text[pos] === "{") {
        const afterBraces = skipBalancedBraces(text, pos);
        if (afterBraces < 0) break;
        pos = afterBraces;
      } else {
        break;
      }

      lastMethod = methodName;
      chainEnd = pos;
    }

    results.push({
      approveOffset,
      chainEndOffset: chainEnd,
      terminalMethod: TERMINAL_METHODS.has(lastMethod) ? lastMethod : null,
    });
  }

  return results;
}

function skipWhitespaceAndComments(text: string, pos: number): number {
  while (pos < text.length) {
    if (/\s/.test(text[pos])) {
      pos++;
    } else if (text[pos] === "/" && pos + 1 < text.length) {
      if (text[pos + 1] === "/") {
        while (pos < text.length && text[pos] !== "\n") pos++;
      } else if (text[pos + 1] === "*") {
        pos += 2;
        while (
          pos + 1 < text.length &&
          !(text[pos] === "*" && text[pos + 1] === "/")
        )
          pos++;
        pos += 2;
      } else {
        break;
      }
    } else {
      break;
    }
  }
  return pos;
}

function skipBalancedParens(text: string, openParenIndex: number): number {
  let depth = 1;
  let i = openParenIndex + 1;
  while (i < text.length && depth > 0) {
    const ch = text[i];
    if (ch === "(") {
      depth++;
    } else if (ch === ")") {
      depth--;
    } else if (ch === '"' || ch === "'") {
      i = skipStringLiteral(text, i);
      continue;
    } else if (ch === "/" && i + 1 < text.length) {
      if (text[i + 1] === "/") {
        while (i < text.length && text[i] !== "\n") i++;
        continue;
      } else if (text[i + 1] === "*") {
        i += 2;
        while (i + 1 < text.length && !(text[i] === "*" && text[i + 1] === "/"))
          i++;
        i += 2;
        continue;
      }
    }
    i++;
  }
  return depth === 0 ? i : -1;
}

function skipBalancedBraces(text: string, openBraceIndex: number): number {
  let depth = 1;
  let i = openBraceIndex + 1;
  while (i < text.length && depth > 0) {
    const ch = text[i];
    if (ch === "{") {
      depth++;
    } else if (ch === "}") {
      depth--;
    } else if (ch === '"' || ch === "'") {
      i = skipStringLiteral(text, i);
      continue;
    } else if (ch === "/" && i + 1 < text.length) {
      if (text[i + 1] === "/") {
        while (i < text.length && text[i] !== "\n") i++;
        continue;
      } else if (text[i + 1] === "*") {
        i += 2;
        while (
          i + 1 < text.length &&
          !(text[i] === "*" && text[i + 1] === "/")
        )
          i++;
        i += 2;
        continue;
      }
    }
    i++;
  }
  return depth === 0 ? i : -1;
}

function skipStringLiteral(text: string, quoteIndex: number): number {
  const quote = text[quoteIndex];

  // Java/Kotlin text block: """..."""
  if (
    quote === '"' &&
    text[quoteIndex + 1] === '"' &&
    text[quoteIndex + 2] === '"'
  ) {
    let i = quoteIndex + 3;
    while (i + 2 < text.length) {
      if (text[i] === '"' && text[i + 1] === '"' && text[i + 2] === '"') {
        return i + 3;
      }
      i++;
    }
    return text.length;
  }

  // Regular string literal
  let i = quoteIndex + 1;
  while (i < text.length && text[i] !== quote) {
    if (text[i] === "\\") i++;
    i++;
  }
  return i + 1;
}

export function findEnclosingMethod(
  text: string,
  offset: number,
): { className: string; methodName: string } | undefined {
  const packageMatch = text.match(/\bpackage\s+([\w.]+)/);
  const packageName = packageMatch ? packageMatch[1] : "";

  const classRegex = /\bclass\s+(\w+)/g;
  let className: string | undefined;
  let classMatch;
  while ((classMatch = classRegex.exec(text)) !== null) {
    if (classMatch.index <= offset) {
      className = classMatch[1];
    }
  }

  if (!className) return undefined;

  const methodRegex = /(?:void|fun)\s+(\w+)\s*\(/g;
  let methodName: string | undefined;
  let methodMatch;
  while ((methodMatch = methodRegex.exec(text)) !== null) {
    if (methodMatch.index <= offset) {
      methodName = methodMatch[1];
    } else {
      break;
    }
  }

  if (!methodName) return undefined;

  const fqcn = packageName ? `${packageName}.${className}` : className;
  return { className: fqcn, methodName };
}
