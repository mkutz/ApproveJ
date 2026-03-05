import * as vscode from "vscode";
import * as path from "path";
import {
  isReceivedFileName,
  isApprovedFileName,
  approvedFileNameCandidates,
  toReceivedFileName,
} from "./fileUtil.js";
import { InventoryManager } from "./inventoryUtil.js";

export function registerCommands(
  context: vscode.ExtensionContext,
  inventory: InventoryManager,
): void {
  context.subscriptions.push(
    vscode.commands.registerCommand(
      "approvej.compareWithApproved",
      async (uri?: vscode.Uri) => {
        const receivedUri = uri ?? vscode.window.activeTextEditor?.document.uri;
        if (!receivedUri) return;

        const approvedUri = await findApprovedFile(receivedUri);
        if (!approvedUri) {
          vscode.window.showWarningMessage(
            "No matching approved file found.",
          );
          return;
        }

        await vscode.commands.executeCommand(
          "vscode.diff",
          approvedUri,
          receivedUri,
          `ApproveJ: ${path.basename(approvedUri.fsPath)} ↔ ${path.basename(receivedUri.fsPath)}`,
        );
      },
    ),

    vscode.commands.registerCommand(
      "approvej.approveReceived",
      async (uri?: vscode.Uri) => {
        const receivedUri = uri ?? vscode.window.activeTextEditor?.document.uri;
        if (!receivedUri) return;

        const approvedUri = await findApprovedFile(receivedUri);
        if (!approvedUri) {
          vscode.window.showWarningMessage(
            "No matching approved file found.",
          );
          return;
        }

        const receivedContent =
          await vscode.workspace.fs.readFile(receivedUri);
        await vscode.workspace.fs.writeFile(approvedUri, receivedContent);
        await vscode.workspace.fs.delete(receivedUri);
      },
    ),

    vscode.commands.registerCommand(
      "approvej.compareWithReceived",
      async (uri?: vscode.Uri) => {
        const approvedUri =
          uri ?? vscode.window.activeTextEditor?.document.uri;
        if (!approvedUri) return;

        const receivedUri = findReceivedFile(approvedUri);
        if (!receivedUri) {
          vscode.window.showWarningMessage(
            "No matching received file found.",
          );
          return;
        }

        await vscode.commands.executeCommand(
          "vscode.diff",
          approvedUri,
          receivedUri,
          `ApproveJ: ${path.basename(approvedUri.fsPath)} ↔ ${path.basename(receivedUri.fsPath)}`,
        );
      },
    ),

    vscode.commands.registerCommand(
      "approvej.navigateToTest",
      async (uri?: vscode.Uri) => {
        const targetUri = uri ?? vscode.window.activeTextEditor?.document.uri;
        if (!targetUri) return;

        let lookupUri = targetUri;
        if (isReceivedFileName(path.basename(targetUri.fsPath))) {
          const approved = await findApprovedFile(targetUri);
          if (approved) lookupUri = approved;
        }

        const testRef = await inventory.findTestReference(lookupUri);
        if (!testRef) {
          vscode.window.showInformationMessage(
            "No test reference found in inventory.",
          );
          return;
        }

        const classSimpleName = testRef.className.split(".").pop();
        const files = await vscode.workspace.findFiles(
          `**/${classSimpleName}.{java,kt}`,
          "{**/build/**,**/target/**,**/out/**,**/node_modules/**}",
        );
        if (files.length === 0) {
          vscode.window.showInformationMessage(
            `Could not find ${classSimpleName}.java or ${classSimpleName}.kt`,
          );
          return;
        }

        const closest = findClosestFile(files, lookupUri);
        const doc = await vscode.workspace.openTextDocument(closest);
        const text = doc.getText();
        const methodRegex = new RegExp(`\\b${testRef.methodName}\\s*\\(`);
        const match = methodRegex.exec(text);

        if (match) {
          const pos = doc.positionAt(match.index);
          const editor = await vscode.window.showTextDocument(doc);
          editor.selection = new vscode.Selection(pos, pos);
          editor.revealRange(new vscode.Range(pos, pos));
        } else {
          await vscode.window.showTextDocument(doc);
        }
      },
    ),

    vscode.commands.registerCommand(
      "approvej.navigateToApprovedFile",
      async (filePath: string) => {
        const uri = vscode.Uri.file(filePath);
        try {
          const doc = await vscode.workspace.openTextDocument(uri);
          await vscode.window.showTextDocument(doc);
        } catch {
          vscode.window.showWarningMessage(
            `Could not open ${path.basename(filePath)}`,
          );
        }
      },
    ),
  );
}

async function findApprovedFile(
  receivedUri: vscode.Uri,
): Promise<vscode.Uri | undefined> {
  const filename = path.basename(receivedUri.fsPath);
  if (!isReceivedFileName(filename)) return undefined;
  const dir = vscode.Uri.joinPath(receivedUri, "..");
  for (const candidate of approvedFileNameCandidates(filename)) {
    const candidateUri = vscode.Uri.joinPath(dir, candidate);
    try {
      await vscode.workspace.fs.stat(candidateUri);
      return candidateUri;
    } catch {
      // File doesn't exist, try next candidate
    }
  }
  return undefined;
}

function findClosestFile(
  candidates: vscode.Uri[],
  referenceUri: vscode.Uri,
): vscode.Uri {
  const refParts = referenceUri.fsPath.split(path.sep);
  let best = candidates[0];
  let bestScore = 0;
  for (const candidate of candidates) {
    const parts = candidate.fsPath.split(path.sep);
    let common = 0;
    while (
      common < parts.length &&
      common < refParts.length &&
      parts[common] === refParts[common]
    ) {
      common++;
    }
    if (common > bestScore) {
      bestScore = common;
      best = candidate;
    }
  }
  return best;
}

function findReceivedFile(
  approvedUri: vscode.Uri,
): vscode.Uri | undefined {
  const filename = path.basename(approvedUri.fsPath);
  if (!isApprovedFileName(filename)) return undefined;
  const receivedName = toReceivedFileName(filename);
  if (!receivedName) return undefined;
  return vscode.Uri.joinPath(approvedUri, "..", receivedName);
}
