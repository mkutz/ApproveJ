import * as vscode from "vscode";
import { InventoryManager } from "./inventoryUtil.js";
import { registerCommands } from "./commands.js";
import { registerEditorNotifications } from "./editorNotifications.js";
import { ApproveCodeLensProvider } from "./codeLensProvider.js";
import {
  DanglingApprovalDiagnosticProvider,
  DanglingApprovalCodeActionProvider,
} from "./diagnosticProvider.js";

export function activate(context: vscode.ExtensionContext): void {
  const inventory = new InventoryManager();
  context.subscriptions.push(inventory);

  registerCommands(context, inventory);
  registerEditorNotifications(context, inventory);

  const javaKotlin: vscode.DocumentFilter[] = [
    { language: "java" },
    { language: "kotlin" },
    { pattern: "**/*.kt" },
    { pattern: "**/*.kts" },
  ];

  context.subscriptions.push(
    vscode.languages.registerCodeLensProvider(
      javaKotlin,
      new ApproveCodeLensProvider(inventory),
    ),
  );

  const diagnosticCollection =
    vscode.languages.createDiagnosticCollection("approvej");
  context.subscriptions.push(diagnosticCollection);
  context.subscriptions.push(
    new DanglingApprovalDiagnosticProvider(diagnosticCollection),
  );

  context.subscriptions.push(
    vscode.languages.registerCodeActionsProvider(
      javaKotlin,
      new DanglingApprovalCodeActionProvider(),
      {
        providedCodeActionKinds:
          DanglingApprovalCodeActionProvider.providedCodeActionKinds,
      },
    ),
  );
}

export function deactivate(): void {
  // Cleanup handled by disposables
}
