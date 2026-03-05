import * as assert from "assert";
import { parseProperties } from "../propertiesParser.js";

describe("inventoryUtil", () => {
  describe("parseProperties", () => {
    it("parses key=value pairs", () => {
      const content =
        "path/to/file-approved.txt=com.example.MyTest#testMethod";
      const result = parseProperties(content);
      assert.strictEqual(result.size, 1);
      assert.strictEqual(
        result.get("path/to/file-approved.txt"),
        "com.example.MyTest#testMethod",
      );
    });

    it("skips comment lines", () => {
      const content = "# This is a comment\nkey=value";
      const result = parseProperties(content);
      assert.strictEqual(result.size, 1);
      assert.strictEqual(result.get("key"), "value");
    });

    it("skips empty lines", () => {
      const content = "key1=value1\n\nkey2=value2";
      const result = parseProperties(content);
      assert.strictEqual(result.size, 2);
    });

    it("handles multiple entries", () => {
      const content = [
        "src/test/file1-approved.txt=com.example.Test1#method1",
        "src/test/file2-approved.json=com.example.Test2#method2",
      ].join("\n");
      const result = parseProperties(content);
      assert.strictEqual(result.size, 2);
      assert.strictEqual(
        result.get("src/test/file1-approved.txt"),
        "com.example.Test1#method1",
      );
      assert.strictEqual(
        result.get("src/test/file2-approved.json"),
        "com.example.Test2#method2",
      );
    });

    it("trims whitespace around key and value", () => {
      const content = "  key  =  value  ";
      const result = parseProperties(content);
      assert.strictEqual(result.get("key"), "value");
    });

    it("skips lines starting with !", () => {
      const content = "! comment\nkey=value";
      const result = parseProperties(content);
      assert.strictEqual(result.size, 1);
    });

    it("skips lines without =", () => {
      const content = "no-separator\nkey=value";
      const result = parseProperties(content);
      assert.strictEqual(result.size, 1);
    });

    it("unescapes backslash-space in keys", () => {
      const content =
        "src/test/BasicsDocTest-approve\\ named-approved.txt = examples.BasicsDocTest#approve named";
      const result = parseProperties(content);
      assert.strictEqual(result.size, 1);
      assert.strictEqual(
        result.get(
          "src/test/BasicsDocTest-approve named-approved.txt",
        ),
        "examples.BasicsDocTest#approve named",
      );
    });

    it("handles colon separator", () => {
      const content = "key:value";
      const result = parseProperties(content);
      assert.strictEqual(result.get("key"), "value");
    });

    it("does not split on escaped equals", () => {
      const content = "key\\=with\\=equals=value";
      const result = parseProperties(content);
      assert.strictEqual(result.get("key=with=equals"), "value");
    });

    it("handles spaces around separator", () => {
      const content =
        "path/to/file-approved.txt = com.example.MyTest#testMethod";
      const result = parseProperties(content);
      assert.strictEqual(
        result.get("path/to/file-approved.txt"),
        "com.example.MyTest#testMethod",
      );
    });
  });
});
