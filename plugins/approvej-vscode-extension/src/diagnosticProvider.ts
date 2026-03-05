import * as vscode from "vscode";
import { findApproveChains } from "./approveChainParser.js";

function isJavaOrKotlin(document: vscode.TextDocument): boolean {
  if (document.languageId === "java" || document.languageId === "kotlin")
    return true;
  const fsPath = document.uri.fsPath;
  return fsPath.endsWith(".kt") || fsPath.endsWith(".kts");
}

export const DIAGNOSTIC_CODE = "approvej.danglingApproval";

export class DanglingApprovalDiagnosticProvider implements vscode.Disposable {
  private readonly diagnosticCollection: vscode.DiagnosticCollection;
  private readonly disposables: vscode.Disposable[] = [];

  constructor(diagnosticCollection: vscode.DiagnosticCollection) {
    this.diagnosticCollection = diagnosticCollection;

    this.disposables.push(
      vscode.workspace.onDidChangeTextDocument((e) =>
        this.updateDiagnostics(e.document),
      ),
      vscode.workspace.onDidOpenTextDocument((doc) =>
        this.updateDiagnostics(doc),
      ),
      vscode.workspace.onDidCloseTextDocument((doc) =>
        this.diagnosticCollection.delete(doc.uri),
      ),
    );

    for (const editor of vscode.window.visibleTextEditors) {
      this.updateDiagnostics(editor.document);
    }
  }

  dispose(): void {
    for (const d of this.disposables) d.dispose();
  }

  private updateDiagnostics(document: vscode.TextDocument): void {
    if (!isJavaOrKotlin(document)) return;

    const text = document.getText();
    const chains = findApproveChains(text);
    const diagnostics: vscode.Diagnostic[] = [];

    for (const chain of chains) {
      if (chain.terminalMethod !== null) continue;

      const startPos = document.positionAt(chain.approveOffset);
      const endPos = document.positionAt(chain.chainEndOffset);
      const range = new vscode.Range(startPos, endPos);

      const diagnostic = new vscode.Diagnostic(
        range,
        "Dangling approval: call by(), byFile(), or byValue() to conclude",
        vscode.DiagnosticSeverity.Warning,
      );
      diagnostic.code = DIAGNOSTIC_CODE;
      diagnostic.source = "ApproveJ";
      diagnostics.push(diagnostic);
    }

    this.diagnosticCollection.set(document.uri, diagnostics);
  }
}

export class DanglingApprovalCodeActionProvider
  implements vscode.CodeActionProvider
{
  static readonly providedCodeActionKinds = [vscode.CodeActionKind.QuickFix];

  provideCodeActions(
    document: vscode.TextDocument,
    _range: vscode.Range,
    context: vscode.CodeActionContext,
  ): vscode.CodeAction[] {
    const diagnostics = context.diagnostics.filter(
      (d) => d.code === DIAGNOSTIC_CODE,
    );

    return diagnostics.flatMap((diagnostic) => [
      this.createFix(document, diagnostic, ".byFile()"),
      this.createFix(document, diagnostic, '.byValue("")'),
    ]);
  }

  private createFix(
    document: vscode.TextDocument,
    diagnostic: vscode.Diagnostic,
    methodCall: string,
  ): vscode.CodeAction {
    const action = new vscode.CodeAction(
      `Conclude with ${methodCall}`,
      vscode.CodeActionKind.QuickFix,
    );
    action.diagnostics = [diagnostic];
    action.isPreferred = methodCall === ".byFile()";

    const edit = new vscode.WorkspaceEdit();
    edit.insert(document.uri, diagnostic.range.end, methodCall);
    action.edit = edit;

    return action;
  }
}