import soot.*;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.StringConstant;
import soot.util.Chain;

import java.io.File;
import java.util.Arrays;

public class Instrument {

	public static class path {
		private static String USER_HOME = System.getenv("ANDROID_HOME");
		private static String androidJar = USER_HOME + "/platforms";
		static String testPath = System.getProperty("user.dir") + File.separator + "testPath";
		static String apkPath = testPath + File.separator + "apk name";
		static String outputPath = testPath + File.separator + "output dir name";
	}

	public static void main(String[] args)  {

		SetUpSoot.setupSoot(path.androidJar, path.apkPath, path.outputPath);

		SootMethod method = injectCode();
		JimpleBody body = Jimple.v().newBody(method);
		method.setActiveBody(body);

		Chain units = body.getUnits();

		Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.printStream"));
		body.getLocals().add(tmpRef);

		units.add(Jimple.v().newAssignStmt(tmpRef, Jimple.v().newStaticFieldRef(
				Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())));

		SootMethod toCall = Scene.v().getMethod("<java.io.PrintStream: void println(java.lang.String)>");
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), StringConstant.v("Hello World"))));
		units.add(Jimple.v().newReturnVoidStmt());

	}


	static SootMethod injectCode(){
		SootClass createClass = createClass();
		return addBindServiceMethod(createClass);
	}

	static SootMethod addBindServiceMethod(SootClass createClass){

		SootMethod bindServiceMethod = new SootMethod("bindService", Arrays.asList(new Type[]{}), VoidType.v() ,Modifier.PUBLIC);
		return bindServiceMethod;
	}

	static SootMethod addAIDL(SootClass createClass){

		SootMethod AIDL = new SootMethod("bindService", Arrays.asList(new Type[]{}), VoidType.v() ,Modifier.PUBLIC);
		return AIDL;
	}

	static SootClass createClass(){
		SootClass ServiceCallClass = new SootClass("ServiceCall", Modifier.PUBLIC);
		ServiceCallClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		ServiceCallClass.setApplicationClass();
		Scene.v().addClass(ServiceCallClass);
		return ServiceCallClass;
	}
}

