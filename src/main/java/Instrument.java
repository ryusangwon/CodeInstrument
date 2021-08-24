import soot.*;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.StringConstant;
import soot.util.Chain;

import java.io.File;
import java.util.Map;

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

		SootClass instrumentClass = createClass();
		SootMethod bindServiceMethod = addBindServiceMethod(instrumentClass);
		SootMethod AIDLMethod = addAIDLMethod(instrumentClass);

		PackManager.v().getPack("jtp").add(
				new Transform("jtp.myLogger", new BodyTransformer() {
					@Override
					protected void internalTransform(Body body, String s, Map<String, String> map) {
						JimpleBody jimpleBody = (JimpleBody) body;
						PatchingChain units = body.getUnits();

					}
				})
		);

	}

	static SootMethod addBindServiceMethod(SootClass instrumentClass){
		SootMethod bindServiceMethod = Scene.v().getMethod("bindService()");
		instrumentClass.addMethod(bindServiceMethod);

		JimpleBody body = Jimple.v().newBody(bindServiceMethod);
		PatchingChain units = body.getUnits();

		Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("Context"));

		units.add(Jimple.v().newAssignStmt(tmpRef, Jimple.v()));

		return bindServiceMethod;
	}

	static SootMethod addAIDLMethod(SootClass instrumentClass){
		SootMethod AIDLMethod = Scene.v().getMethod("serviceThreadStart()");
		instrumentClass.addMethod(AIDLMethod);

		JimpleBody body = Jimple.v().newBody(AIDLMethod);
		PatchingChain units = body.getUnits();

		Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("IServiceInterface"));

		units.add(Jimple.v().newLocal(tmpRef, Jimple.v()))

		return AIDLMethod;
	}


	static SootClass createClass(){
		SootClass instrumentClass = new SootClass("InstrumentClass", Modifier.PUBLIC);
		instrumentClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
		instrumentClass.setApplicationClass();
		Scene.v().addClass(instrumentClass);
		return instrumentClass;
	}
}

