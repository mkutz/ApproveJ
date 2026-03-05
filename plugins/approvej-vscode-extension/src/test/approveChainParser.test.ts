import * as assert from "assert";
import {
  findApproveChains,
  hasApproveJImport,
  findEnclosingMethod,
} from "../approveChainParser.js";

describe("approveChainParser", () => {
  describe("hasApproveJImport", () => {
    it("detects static import", () => {
      assert.strictEqual(
        hasApproveJImport(
          "import static org.approvej.ApprovalBuilder.approve;",
        ),
        true,
      );
    });

    it("detects regular import", () => {
      assert.strictEqual(
        hasApproveJImport("import org.approvej.ApprovalBuilder;"),
        true,
      );
    });

    it("returns false without import", () => {
      assert.strictEqual(
        hasApproveJImport("import com.example.Other;"),
        false,
      );
    });
  });

  describe("findApproveChains", () => {
    const importLine =
      "import static org.approvej.ApprovalBuilder.approve;\n";

    it("finds chain with byFile terminal", () => {
      const text = importLine + 'approve("value").byFile();';
      const chains = findApproveChains(text);
      assert.strictEqual(chains.length, 1);
      assert.strictEqual(chains[0].terminalMethod, "byFile");
    });

    it("finds chain with byValue terminal", () => {
      const text = importLine + 'approve("value").byValue("expected");';
      const chains = findApproveChains(text);
      assert.strictEqual(chains.length, 1);
      assert.strictEqual(chains[0].terminalMethod, "byValue");
    });

    it("finds chain with by terminal", () => {
      const text = importLine + 'approve("value").by(myApprover);';
      const chains = findApproveChains(text);
      assert.strictEqual(chains.length, 1);
      assert.strictEqual(chains[0].terminalMethod, "by");
    });

    it("finds dangling chain without terminal", () => {
      const text = importLine + 'approve("value").printedAs(json());';
      const chains = findApproveChains(text);
      assert.strictEqual(chains.length, 1);
      assert.strictEqual(chains[0].terminalMethod, null);
    });

    it("finds chain with intermediate methods", () => {
      const text =
        importLine +
        'approve(result).printedAs(json()).scrubbedOf(dateTime()).byFile();';
      const chains = findApproveChains(text);
      assert.strictEqual(chains.length, 1);
      assert.strictEqual(chains[0].terminalMethod, "byFile");
    });

    it("handles multi-line chain", () => {
      const text =
        importLine +
        "approve(result)\n    .printedAs(json())\n    .byFile();";
      const chains = findApproveChains(text);
      assert.strictEqual(chains.length, 1);
      assert.strictEqual(chains[0].terminalMethod, "byFile");
    });

    it("returns empty for files without approvej import", () => {
      const text = 'approve("value").byFile();';
      const chains = findApproveChains(text);
      assert.strictEqual(chains.length, 0);
    });

    it("finds multiple chains", () => {
      const text =
        importLine +
        'approve("a").byFile();\napprove("b").byValue("expected");';
      const chains = findApproveChains(text);
      assert.strictEqual(chains.length, 2);
    });

    it("handles bare approve call", () => {
      const text = importLine + 'approve("value");';
      const chains = findApproveChains(text);
      assert.strictEqual(chains.length, 1);
      assert.strictEqual(chains[0].terminalMethod, null);
    });

    it("handles text block inside lambda argument", () => {
      const text =
        importLine +
        [
          "approve(person)",
          "    .printedBy(",
          "        it ->",
          '            """',
          "            person:",
          '              name: \\"%s\\"',
          "            \"\"\"",
          "                .formatted(it.name())) // <1>",
          '    .byFile(nextToTest().filenameExtension("yml"));',
        ].join("\n");
      const chains = findApproveChains(text);
      assert.strictEqual(chains.length, 1);
      assert.strictEqual(chains[0].terminalMethod, "byFile");
    });

    it("handles Kotlin raw string inside argument", () => {
      const text =
        importLine +
        [
          'approve(result).printedBy { """{"key": "$it"}""" }.byFile();',
        ].join("\n");
      const chains = findApproveChains(text);
      assert.strictEqual(chains.length, 1);
      assert.strictEqual(chains[0].terminalMethod, "byFile");
    });
  });

  describe("findEnclosingMethod", () => {
    it("finds Java void method", () => {
      const text = [
        "package com.example;",
        "class MyTest {",
        "  void myMethod() {",
        '    approve("value").byFile();',
        "  }",
        "}",
      ].join("\n");

      const offset = text.indexOf("approve");
      const result = findEnclosingMethod(text, offset);
      assert.deepStrictEqual(result, {
        className: "com.example.MyTest",
        methodName: "myMethod",
      });
    });

    it("finds Kotlin fun method", () => {
      const text = [
        "package com.example",
        "class MyTest {",
        "  fun myMethod() {",
        '    approve("value").byFile()',
        "  }",
        "}",
      ].join("\n");

      const offset = text.indexOf("approve");
      const result = findEnclosingMethod(text, offset);
      assert.deepStrictEqual(result, {
        className: "com.example.MyTest",
        methodName: "myMethod",
      });
    });

    it("returns undefined without class", () => {
      const text = '  void myMethod() {\n    approve("value");\n  }';
      const offset = text.indexOf("approve");
      const result = findEnclosingMethod(text, offset);
      assert.strictEqual(result, undefined);
    });

    it("returns undefined without method", () => {
      const text = 'class MyTest {\n  approve("value");\n}';
      const offset = text.indexOf("approve");
      const result = findEnclosingMethod(text, offset);
      assert.strictEqual(result, undefined);
    });
  });
});
