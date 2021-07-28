import soot.*;
import soot.jimple.JasminClass;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.StringConstant;
import soot.options.Options;
import soot.util.Chain;
import soot.util.JasminOutputStream;

import java.io.*;
import java.util.Arrays;

public class Main {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Scene.v().loadClassAndSupport("java.lang.Object");
		Scene.v().loadClassAndSupport("java.lang.System");

		SootClass sClass = new SootClass("HelloWorld", Modifier.PUBLIC);
		sClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		Scene.v().addClass(sClass);

		SootMethod method = new SootMethod("main",
				Arrays.asList(new Type[] {ArrayType.v(RefType.v("java.lang.String"), 1)}),
				VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
		sClass.addMethod(method);

		JimpleBody body = Jimple.v().newBody(method);
		method.setActiveBody(body);
		Chain units = body.getUnits();

		Local arg = Jimple.v().newLocal("10", ArrayType.v(RefType.v("java.lang.string"), 1));
		body.getLocals().add(arg);

		Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.printStream"));
		body.getLocals().add(tmpRef);

		units.add(Jimple.v().newIdentityStmt(arg,
				Jimple.v().newParameterRef(ArrayType.v(RefType.v("java.lang.String"), 1), 0)));

		units.add(Jimple.v().newAssignStmt(tmpRef, Jimple.v().newStaticFieldRef(
				Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())));

		SootMethod toCall = Scene.v().getMethod("<java.io.PrintStream: void println(java.lang.String)>");
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), StringConstant.v("Hello World"))));
		units.add(Jimple.v().newReturnVoidStmt());

		String fileName = SourceLocator.v().getFileNameFor(sClass, Options.output_format_class);
		OutputStream streamOut = new JasminOutputStream(
				new FileOutputStream(fileName));
		PrintWriter writerOut = new PrintWriter(
				new OutputStreamWriter(streamOut));

		JasminClass jasminClass = new soot.jimple.JasminClass(sClass);
		writerOut.flush();
		streamOut.close();

	}


}

