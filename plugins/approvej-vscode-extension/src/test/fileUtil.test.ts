import * as assert from "assert";
import {
  isApprovedFileName,
  isReceivedFileName,
  toApprovedFileName,
  toReceivedFileName,
  toBaseFileName,
  approvedFileNameCandidates,
} from "../fileUtil.js";

describe("fileUtil", () => {
  describe("isApprovedFileName", () => {
    it("returns true for filename with -approved before extension", () => {
      assert.strictEqual(isApprovedFileName("test-approved.txt"), true);
    });

    it("returns true for filename with -approved without extension", () => {
      assert.strictEqual(isApprovedFileName("test-approved"), true);
    });

    it("returns true for filename with -approved and compound name", () => {
      assert.strictEqual(
        isApprovedFileName("my-test-approved.json"),
        true,
      );
    });

    it("returns false for filename starting with -approved", () => {
      assert.strictEqual(isApprovedFileName("-approved.txt"), false);
    });

    it("returns false for filename without -approved", () => {
      assert.strictEqual(isApprovedFileName("test.txt"), false);
    });

    it("returns false for -approved followed by non-dot character", () => {
      assert.strictEqual(isApprovedFileName("test-approvedx.txt"), false);
    });
  });

  describe("isReceivedFileName", () => {
    it("returns true for filename with -received before extension", () => {
      assert.strictEqual(isReceivedFileName("test-received.txt"), true);
    });

    it("returns true for filename with -received without extension", () => {
      assert.strictEqual(isReceivedFileName("test-received"), true);
    });

    it("returns false for filename starting with -received", () => {
      assert.strictEqual(isReceivedFileName("-received.txt"), false);
    });

    it("returns false for filename without -received", () => {
      assert.strictEqual(isReceivedFileName("test.txt"), false);
    });
  });

  describe("toApprovedFileName", () => {
    it("converts received filename to approved", () => {
      assert.strictEqual(
        toApprovedFileName("test-received.txt"),
        "test-approved.txt",
      );
    });

    it("converts received filename without extension", () => {
      assert.strictEqual(
        toApprovedFileName("test-received"),
        "test-approved",
      );
    });

    it("returns undefined for non-received filename", () => {
      assert.strictEqual(toApprovedFileName("test.txt"), undefined);
    });
  });

  describe("toReceivedFileName", () => {
    it("converts approved filename to received", () => {
      assert.strictEqual(
        toReceivedFileName("test-approved.txt"),
        "test-received.txt",
      );
    });

    it("returns undefined for non-approved filename", () => {
      assert.strictEqual(toReceivedFileName("test.txt"), undefined);
    });
  });

  describe("toBaseFileName", () => {
    it("removes -received infix", () => {
      assert.strictEqual(toBaseFileName("test-received.txt"), "test.txt");
    });

    it("removes -received infix without extension", () => {
      assert.strictEqual(toBaseFileName("test-received"), "test");
    });

    it("returns undefined for non-received filename", () => {
      assert.strictEqual(toBaseFileName("test.txt"), undefined);
    });
  });

  describe("approvedFileNameCandidates", () => {
    it("returns approved and base names", () => {
      assert.deepStrictEqual(
        approvedFileNameCandidates("test-received.txt"),
        ["test-approved.txt", "test.txt"],
      );
    });

    it("returns empty array for non-received filename", () => {
      assert.deepStrictEqual(approvedFileNameCandidates("test.txt"), []);
    });
  });
});
