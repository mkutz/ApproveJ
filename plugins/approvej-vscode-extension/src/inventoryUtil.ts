import * as vscode from "vscode";
import * as path from "path";
import {
  type TestReference,
  parseProperties,
  parseTestReference,
} from "./propertiesParser.js";

export type { TestReference };
export { parseProperties };

export class InventoryManager implements vscode.Disposable {
  private cache: Map<string, string> | null = null;
  private watcher: vscode.FileSystemWatcher;

  constructor() {
    this.watcher = vscode.workspace.createFileSystemWatcher(
      "**/.approvej/inventory.properties",
    );
    this.watcher.onDidChange(() => (this.cache = null));
    this.watcher.onDidCreate(() => (this.cache = null));
    this.watcher.onDidDelete(() => (this.cache = null));
  }

  dispose(): void {
    this.watcher.dispose();
  }

  async getInventory(): Promise<Map<string, string>> {
    if (this.cache) return this.cache;
    this.cache = await this.loadInventory();
    return this.cache;
  }

  async findTestReference(
    fileUri: vscode.Uri,
  ): Promise<TestReference | undefined> {
    const inventory = await this.getInventory();
    const relativePath = this.toWorkspaceRelativePath(fileUri);
    if (!relativePath) return undefined;
    const value = inventory.get(relativePath);
    return value ? parseTestReference(value) : undefined;
  }

  async findApprovedFiles(
    className: string,
    methodName: string,
  ): Promise<vscode.Uri[]> {
    const inventory = await this.getInventory();
    const testReference = `${className}#${methodName}`;
    const workspaceFolder = vscode.workspace.workspaceFolders?.[0];
    if (!workspaceFolder) return [];

    const results: vscode.Uri[] = [];
    for (const [key, value] of inventory) {
      if (value === testReference) {
        results.push(vscode.Uri.joinPath(workspaceFolder.uri, key));
      }
    }
    return results;
  }

  private toWorkspaceRelativePath(uri: vscode.Uri): string | undefined {
    const workspaceFolder = vscode.workspace.getWorkspaceFolder(uri);
    if (!workspaceFolder) return undefined;
    return path.relative(workspaceFolder.uri.fsPath, uri.fsPath);
  }

  private async loadInventory(): Promise<Map<string, string>> {
    const merged = new Map<string, string>();
    const files = await vscode.workspace.findFiles(
      "**/.approvej/inventory.properties",
      "{**/build/**,**/target/**,**/out/**,**/node_modules/**}",
    );
    for (const file of files) {
      await this.loadAndMerge(file, merged);
    }
    return merged;
  }

  private async loadAndMerge(
    inventoryFile: vscode.Uri,
    merged: Map<string, string>,
  ): Promise<void> {
    const workspaceFolder = vscode.workspace.getWorkspaceFolder(inventoryFile);
    if (!workspaceFolder) return;

    const moduleDir = vscode.Uri.joinPath(inventoryFile, "..", "..");
    const prefix = path.relative(
      workspaceFolder.uri.fsPath,
      moduleDir.fsPath,
    );

    try {
      const content = await vscode.workspace.fs.readFile(inventoryFile);
      const text = Buffer.from(content).toString("utf-8");
      const props = parseProperties(text);

      for (const [key, value] of props) {
        const projectRelativeKey =
          prefix && prefix !== "." ? `${prefix}/${key}` : key;
        merged.set(projectRelativeKey, value);
      }
    } catch {
      // Ignore files that can't be read
    }
  }
}
