import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Instrument {

	private static Object TAG = "<soot>";

	public static class path {
		private static String USER_HOME = System.getenv("ANDROID_HOME");
		private static String androidJar = USER_HOME + "/platforms";
		static String testPath = System.getProperty("user.dir") + File.separator + "apkdir";
		static String apkPath = testPath + File.separator + "sootTest.apk";
		static String outputPath = testPath + File.separator + "Instrumented";
	}

	public static void main(String[] args)  {

		String TAG = "<Instrumented>";

		SetUpSoot.setupSoot(path.androidJar, path.apkPath, path.outputPath);

		PackManager.v().getPack("jtp").add(
				new Transform("jtp.myLogger", new BodyTransformer() {
					@Override
					protected void internalTransform(Body body, String s, Map<String, String> map) {
						JimpleBody jimpleBody = (JimpleBody) body;
						PatchingChain units = body.getUnits();

						SootClass instrumentClass = createClass();
						List<Unit> bindServiceUnits = addBindServiceMethod(instrumentClass, jimpleBody);
						List<Unit> AIDLUnits = addAIDLMethod(instrumentClass, jimpleBody);

						units.insertBefore(bindServiceUnits, ((JimpleBody) body).getFirstNonIdentityStmt());
						units.insertAfter(AIDLUnits, (Unit) bindServiceUnits);

						jimpleBody.validate();
					}
				})
		);
		soot.Main.main(args);
		PackManager.v().runPacks();
		PackManager.v().writeOutput();
	}

	static List addBindServiceMethod(SootClass instrumentClass, Body body){

		SootMethod bindServiceMethod = Scene.v().getMethod("bindService()");
		instrumentClass.addMethod(bindServiceMethod);

		List<Unit> generatedUnits = new ArrayList<>();

		Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("Context"));
		body.getLocals().add(tmpRef);
		SootField bindServiceField = Scene.v().getField("Context");

		AssignStmt bindServiceAssignStmt = Jimple.v().newAssignStmt(tmpRef, Jimple.v().newStaticFieldRef(
				bindServiceField.makeRef()));
		InvokeStmt bindServiceStmt = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tmpRef, bindServiceMethod.makeRef()));

		generatedUnits.addAll(SetUpSoot.generateLogStmts((JimpleBody) body, "BindService method : " + body.getMethod().getSignature()));
		generatedUnits.add(bindServiceAssignStmt);
		generatedUnits.add(bindServiceStmt);


		return generatedUnits;
	}

	static List addAIDLMethod(SootClass instrumentClass, Body body){
		SootMethod AIDLMethod = Scene.v().getMethod("serviceThreadStart()");
		instrumentClass.addMethod(AIDLMethod);

		List<Unit> generatedUnits = new ArrayList<>();

		Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("IServiceInterface"));
		body.getLocals().add(tmpRef);
		SootField AIDLField = Scene.v().getField("IServiceInterface");

		AssignStmt AIDLAssignStmt = Jimple.v().newAssignStmt(tmpRef, Jimple.v().newStaticFieldRef(
				AIDLField.makeRef()));
		InvokeStmt AIDLStmt = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tmpRef, AIDLMethod.makeRef()));

		generatedUnits.addAll(SetUpSoot.generateLogStmts((JimpleBody) body, "AIDL method : " + body.getMethod().getSignature()));
		generatedUnits.add(AIDLAssignStmt);
		generatedUnits.add(AIDLStmt);

		return generatedUnits;
	}


	static SootClass createClass(){
		SootClass instrumentClass = new SootClass("InstrumentClass", Modifier.PUBLIC);
		instrumentClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		instrumentClass.setApplicationClass();
		Scene.v().addClass(instrumentClass);
		return instrumentClass;
	}
}

