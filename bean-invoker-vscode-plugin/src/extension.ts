import * as vscode from 'vscode';
import * as path from 'path';
import net from 'net';
import { parse, stringify, CommentArray, CommentObject, CommentJSONValue } from 'comment-json';

export function activate(context: vscode.ExtensionContext) {
	const genarateBeanInvokerVmargsCommand = vscode.commands.registerCommand("spring-bean-invoker.GenarateBeanInvokerVmargs", () => {
		const activeTextEditor = vscode.window.activeTextEditor
		if (!activeTextEditor) {
			return
		}
		const launchObj = <CommentObject>parse(activeTextEditor.document.getText())
		if (!launchObj) {
			return
		}

		const extensionPath = vscode.extensions.getExtension("newhoo.spring-bean-invoker")?.extensionPath
		const agentPath = extensionPath + path.sep + "jars" + path.sep + "bean-invoker-agent.jar"

		let upsertVmArgs = false
		const availablePort = findAvailablePort()
		const configurations = <CommentArray<CommentObject>>launchObj['configurations']
		configurations.forEach(configuration => {
			if (configuration['type'] === 'java' && configuration['request'] === 'launch') {
				let vmArgsArr: string[] | CommentArray<string> = []
				if (configuration['vmArgs']) {
					const v = configuration['vmArgs']
					if (typeof v === 'string') {
						const s = v.trim().replaceAll("  ", " ")
						if (s) {
							vmArgsArr = s.split(" ")
						}
					} else if (v instanceof CommentArray) {
						vmArgsArr = <CommentArray<string>>v
					} else {
						return v
					}

					let a, b
					vmArgsArr = vmArgsArr.map((s: string) => {
						if (s.startsWith("-javaagent:" + extensionPath)) {
							a = true
							return `-javaagent:${agentPath}`;
						}
						if (s.startsWith("-Dspring.invokePort=")) {
							b = true
							return `-Dspring.invokePort=${availablePort}`;
						}
						return s
					})
					if (!a) {
						vmArgsArr.push(`-javaagent:${agentPath}`);
					}
					if (!b) {
						vmArgsArr.push(`-Dspring.invokePort=${availablePort}`);
					}
				} else {
					vmArgsArr.push(`-javaagent:${agentPath}`);
					vmArgsArr.push(`-Dspring.invokePort=${availablePort}`);
				}
				upsertVmArgs = true
				configuration['vmArgs'] = <CommentJSONValue>vmArgsArr
			}
		})

		if (upsertVmArgs) {
			activeTextEditor.edit(editBuilder => {
				const end = new vscode.Position(activeTextEditor.document.lineCount + 1, 0);
				editBuilder.replace(new vscode.Range(new vscode.Position(0, 0), end), stringify(launchObj, null, 4));
			});
		} else {
			vscode.window.showInformationMessage("Not found or recognize java launch configuration.");
		}
	});
	context.subscriptions.push(genarateBeanInvokerVmargsCommand);

	const invokeBeanCommand = vscode.commands.registerCommand("spring-bean-invoker.InvokeBean", async () => {
		const classMethod = findClassMethod()
		if (!classMethod) {
			return
		}

		const debugSession = vscode.debug.activeDebugSession
		if (!debugSession) {
			vscode.window.showInformationMessage("App not start.");
			return
		}
		const port = findPortFromDebugSession(debugSession);

		const socket = new net.Socket();
		socket.setTimeout(3000) //3s
		socket.setKeepAlive(false)
		socket.setEncoding('utf-8');
		socket.connect(port, '127.0.0.1', function () {
			socket.write(classMethod + "\r\n");
		});
		socket.on('error', function (error) {
			vscode.window.showErrorMessage("Invoke bean method error: " + error.message);
			console.log('Invoke bean method error: ' + error);
		});
		// socket.on('close', function () {
		// 	vscode.window.showErrorMessage("Invoke bean method error: server close");
		// });
		socket.on('timeout', function () {
			vscode.window.showErrorMessage("Invoke bean method error: timeout");
		});
	});

	context.subscriptions.push(invokeBeanCommand);
}

function findClassMethod(): string | undefined {
	const activeTextEditor = vscode.window.activeTextEditor
	if (!activeTextEditor) {
		return
	}

	const lineText = activeTextEditor.document.lineAt(activeTextEditor.selection.active.line).text
	if (!lineText || lineText.indexOf(";") > -1 || lineText.indexOf('(') < 0 || lineText.substring(0, lineText.indexOf('(')).trim().indexOf(' ') < 0) {
		vscode.window.showWarningMessage("Your should click on the java method signature line.");
		return
	}
	const methodNames = lineText.match(/\s+([a-zA-Z0-9$]+)\s*\(\s*\)/)
	if (!methodNames || methodNames.length < 1) {
		vscode.window.showInformationMessage("Spring bean method with no parameter not found. Try to generate ?", 'Yes', 'No').then(choice => {
			if (choice === 'Yes') {
				const generateMethodNames = lineText.match(/\s+([a-zA-Z0-9$]+)\s*\(\s*/)
				if (!generateMethodNames || generateMethodNames.length < 1) {
					vscode.window.showWarningMessage("Generate bean method name not found: " + lineText);
					return
				}
				const targetMethodName = generateMethodNames[generateMethodNames.length - 1]

				const insertMethod = `
	// TODO clean: ${new Date()}
    public void ${targetMethodName + "TEST"}() throws Exception {
        ${targetMethodName}();
        System.out.println();
    }
`

				let targetLine = activeTextEditor.selection.start.line - 1
				while (targetLine >= 0 && activeTextEditor.document.lineAt(targetLine).text.trim() !== '') {
					targetLine--
				}
				activeTextEditor.edit(editBuilder => {
					const targetLinePos = new vscode.Position(targetLine, 0);
					editBuilder.replace(new vscode.Range(targetLinePos, targetLinePos), insertMethod);
				});
			}

		})
		// const choice = await vscode.window.showInformationMessage("Spring bean method with no parameter not found. Try generate ?", 'Yes', 'No');
		return
	}
	const methodName = methodNames[methodNames.length - 1]

	const doc = activeTextEditor.document.getText()
	const packageNames = doc.match(/\s*package\s+([0-9a-zA-Z$.]+);$/m)
	if (!packageNames || packageNames.length < 1) {
		vscode.window.showWarningMessage("Java class package not found in current file.");
		return
	}
	const packageName = packageNames[packageNames.length - 1]

	const classNames = doc.match(/\s*public\s+class\s+([a-zA-Z$][0-9a-zA-Z$]*)+\s+/m)
	if (!classNames || classNames.length < 1) {
		vscode.window.showWarningMessage("Java class name not found in current file.");
		return
	}
	const className = classNames[classNames.length - 1]

	return packageName + "." + className + "::" + methodName
}

function findAvailablePort(): number {
	return 6666 + Math.floor(Math.random() * 100 + 8)
}

function findPortFromDebugSession(debugSession: vscode.DebugSession): number {
	let port = "0"
	const vmArgs = <string>debugSession.configuration['vmArgs']
	if (vmArgs) {
		const vmArgsArr = vmArgs.split(" ")
		vmArgsArr.forEach((s: string) => {
			if (s.startsWith("-Dspring.invokePort=")) {
				port = s.substring('-Dspring.invokePort='.length);
			}
		})
	}
	return parseInt(port)
}
``
export function deactivate() { }
