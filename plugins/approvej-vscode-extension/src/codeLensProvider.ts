import * as vscode from "vscode";
import * as path from "path";
import { findApproveChains, findEnclosingMethod } from "./approveChainParser.js";
import { isApprovedFileName, toReceivedFileName } from "./fileUtil.js";
import { InventoryManager } from "./inventoryUtil.js";

export class ApproveCodeLensProvider implements vscode.CodeLensProvider {
  private readonly inventory: InventoryManager;

  constructor(inventory: InventoryManager) {
    this.inventory = inventory;
  }

  async provideCodeLenses(
    document: vscode.TextDocument,
  ): Promise<vscode.CodeLens[]> {
    const text = document.getText();
    const chains = findApproveChains(text);
    const codeLenses: vscode.CodeLens[] = [];

    for (const chain of chains) {
      if (chain.terminalMethod !== "byFile") continue;

      const pos = document.positionAt(chain.approveOffset);
      const range = new vscode.Range(pos, pos);

      const enclosing = findEnclosingMethod(text, chain.approveOffset);
      if (!enclosing) continue;

      const approvedFiles = await this.inventory.findApprovedFiles(
        enclosing.className,
        enclosing.methodName,
      );

      if (approvedFiles.length === 0) continue;

      for (const approvedUri of approvedFiles) {
        const approvedName = path.basename(approvedUri.fsPath);

        const hasReceived = await this.checkForReceivedFile(approvedUri);

        codeLenses.push(
          new vscode.CodeLens(range, {
            title: `$(file) ${approvedName}`,
            command: "approvej.navigateToApprovedFile",
            arguments: [approvedUri.fsPath],
          }),
        );

        if (hasReceived) {
          codeLenses.push(
            new vscode.CodeLens(range, {
              title: "$(diff) Compare received and approved",
              command: "approvej.compareWithReceived",
              arguments: [approvedUri],
            }),
          );
        }
      }
    }

    return codeLenses;
  }

  private async checkForReceivedFile(
    approvedUri: vscode.Uri,
  ): Promise<boolean> {
    const filename = path.basename(approvedUri.fsPath);
    if (!isApprovedFileName(filename)) return false;
    const receivedName = toReceivedFileName(filename);
    if (!receivedName) return false;
    const receivedUri = vscode.Uri.joinPath(approvedUri, "..", receivedName);
    try {
      await vscode.workspace.fs.stat(receivedUri);
      return true;
    } catch {
      return false;
    }
  }
}