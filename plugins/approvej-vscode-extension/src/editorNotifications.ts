import * as vscode from "vscode";
import * as path from "path";
import {
  isReceivedFileName,
  isApprovedFileName,
  approvedFileNameCandidates,
  toReceivedFileName,
} from "./fileUtil.js";
import { InventoryManager } from "./inventoryUtil.js";

export function registerEditorNotifications(
  context: vscode.ExtensionContext,
  inventory: InventoryManager,
): void {
  const statusBarItem = vscode.window.createStatusBarItem(
    vscode.StatusBarAlignment.Right,
    100,
  );
  context.subscriptions.push(statusBarItem);

  context.subscriptions.push(
    vscode.window.onDidChangeActiveTextEditor((editor) =>
      updateStatusBar(editor, statusBarItem),
    ),
  );
  updateStatusBar(vscode.window.activeTextEditor, statusBarItem);

  context.subscriptions.push(
    vscode.languages.registerCodeLensProvider(
      { scheme: "file" },
      new FileNotificationCodeLensProvider(inventory),
    ),
  );
}

function updateStatusBar(
  editor: vscode.TextEditor | undefined,
  statusBarItem: vscode.StatusBarItem,
): void {
  if (!editor) {
    statusBarItem.hide();
    return;
  }

  const filename = path.basename(editor.document.uri.fsPath);
  if (isReceivedFileName(filename)) {
    statusBarItem.text = "$(testing-error-icon) ApproveJ: Received";
    statusBarItem.tooltip =
      "This is an ApproveJ received file that has not been approved yet";
    statusBarItem.show();
  } else if (isApprovedFileName(filename)) {
    statusBarItem.text = "$(pass) ApproveJ: Approved";
    statusBarItem.tooltip = "This is an ApproveJ approved file";
    statusBarItem.show();
  } else {
    statusBarItem.hide();
  }
}

class FileNotificationCodeLensProvider implements vscode.CodeLensProvider {
  private readonly inventory: InventoryManager;

  constructor(inventory: InventoryManager) {
    this.inventory = inventory;
  }

  async provideCodeLenses(
    document: vscode.TextDocument,
  ): Promise<vscode.CodeLens[]> {
    const filename = path.basename(document.uri.fsPath);
    const range = new vscode.Range(0, 0, 0, 0);

    if (isReceivedFileName(filename)) {
      return this.receivedFileLenses(document.uri, filename, range);
    } else if (isApprovedFileName(filename)) {
      return this.approvedFileLenses(document.uri, filename, range);
    }
    return [];
  }

  private async receivedFileLenses(
    uri: vscode.Uri,
    filename: string,
    range: vscode.Range,
  ): Promise<vscode.CodeLens[]> {
    const lenses: vscode.CodeLens[] = [];
    const approvedUri = await this.findApprovedFile(uri, filename);

    if (approvedUri) {
      lenses.push(
        new vscode.CodeLens(range, {
          title:
            "$(warning) ApproveJ: This received file has not been approved yet",
          command: "",
        }),
        new vscode.CodeLens(range, {
          title: "$(diff) Compare with Approved",
          command: "approvej.compareWithApproved",
          arguments: [uri],
        }),
        new vscode.CodeLens(range, {
          title: "$(check) Approve",
          command: "approvej.approveReceived",
          arguments: [uri],
        }),
      );

      const testRef = await this.inventory.findTestReference(approvedUri);
      if (testRef) {
        lenses.push(
          new vscode.CodeLens(range, {
            title: "$(symbol-method) Navigate to Test",
            command: "approvej.navigateToTest",
            arguments: [uri],
          }),
        );
      }
    } else {
      lenses.push(
        new vscode.CodeLens(range, {
          title:
            "$(warning) ApproveJ: This is a received file. No matching approved file was found nearby.",
          command: "",
        }),
      );
    }

    return lenses;
  }

  private async approvedFileLenses(
    uri: vscode.Uri,
    filename: string,
    range: vscode.Range,
  ): Promise<vscode.CodeLens[]> {
    const lenses: vscode.CodeLens[] = [];

    lenses.push(
      new vscode.CodeLens(range, {
        title: "$(pass) ApproveJ: Approved file",
        command: "",
      }),
    );

    const receivedName = toReceivedFileName(filename);
    if (receivedName) {
      const receivedUri = vscode.Uri.joinPath(uri, "..", receivedName);
      try {
        await vscode.workspace.fs.stat(receivedUri);
        lenses.push(
          new vscode.CodeLens(range, {
            title: "$(diff) Compare with Received",
            command: "approvej.compareWithReceived",
            arguments: [uri],
          }),
        );
      } catch {
        // No received file
      }
    }

    const testRef = await this.inventory.findTestReference(uri);
    if (testRef) {
      lenses.push(
        new vscode.CodeLens(range, {
          title: "$(symbol-method) Navigate to Test",
          command: "approvej.navigateToTest",
          arguments: [uri],
        }),
      );
    }

    return lenses;
  }

  private async findApprovedFile(
    receivedUri: vscode.Uri,
    filename: string,
  ): Promise<vscode.Uri | undefined> {
    const dir = vscode.Uri.joinPath(receivedUri, "..");
    for (const candidate of approvedFileNameCandidates(filename)) {
      const candidateUri = vscode.Uri.joinPath(dir, candidate);
      try {
        await vscode.workspace.fs.stat(candidateUri);
        return candidateUri;
      } catch {
        // File doesn't exist
      }
    }
    return undefined;
  }
}
