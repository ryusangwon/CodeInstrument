import soot.*;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.StringConstant;
import soot.util.Chain;

import java.io.File;
import java.util.Arrays;

public class Instrument {

	private static String USER_HOME = System.getenv("ANDROID_HOME");
	private static String androidJar = USER_HOME + "/platforms";
	static String testPath = System.getProperty("user.dir") + File.separator + "testPath";
	static String apkPath = testPath + File.separator + "apk name";
	static String outputPath = test + File.separator + "output dir name";

	public static void main(String[] args)  {

		SetUpSoot.setupSoot(androidJar, apkPath, outputPath);

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



	}


}

