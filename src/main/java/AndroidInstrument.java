import java.util.Iterator;
import java.util.Map;

import soot.*;
import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.*;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AndroidInstrument {

    private final static String USER_HOME = System.getenv("ANDROID_HOME");
    private static String androidJar = USER_HOME + "/platforms";
    static String testPath = System.getProperty("user.dir") + File.separator + "apkdir";
    static String apkPath = testPath + File.separator + "beforeInstrument.apk";
    static String outputPath = testPath + File.separator + "/Instrumented";

    public static void setupSoot(String androidJar, String apkPath, String outputPath) {
        G.reset();
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_whole_program(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_validate(true);
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_format(Options.output_format_dex);
        Options.v().set_android_jars(androidJar);
        Options.v().set_process_dir(Collections.singletonList(apkPath));
        Options.v().set_include_all(true);
        Options.v().set_process_multiple_dex(true);
        Options.v().set_output_dir(outputPath);
        Scene.v().addBasicClass("java.io.PrintStream", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
        Scene.v().loadNecessaryClasses();
    }

    public static void main(String[] args) {

        setupSoot(androidJar, apkPath, outputPath);

        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {

            @Override
            protected void internalTransform(final Body b, String phaseName, Map<String, String> options) {
                JimpleBody body = (JimpleBody) b;
                PatchingChain units = b.getUnits();
                List<Unit> generatedUnits = new ArrayList<>();

                Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
                body.getLocals().add(tmpRef);
                SootField sysOutField = Scene.v().getField("<java.lang.System: java.io.PrintStream out>");
                AssignStmt sysOutAssignStmt = Jimple.v().newAssignStmt(tmpRef, Jimple.v().newStaticFieldRef(sysOutField.makeRef()));
                generatedUnits.add(sysOutAssignStmt);

                Local tmpString = Jimple.v().newLocal("tmpString", RefType.v("java.lang.String"));
                body.getLocals().add(tmpString);
                Value printlnParameter = StringConstant.v("Hello, World!");
                SootMethod printlnMethod = Scene.v().grabMethod("<java.io.PrintStream: void println(java.lang.String)>");
                InvokeStmt printlnMethodCallStmt = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tmpRef, printlnMethod.makeRef(), printlnParameter));
                generatedUnits.add(printlnMethodCallStmt);


                b.validate();
            }
        }));
        soot.Main.main(args);
        PackManager.v().runPacks();
        PackManager.v().writeOutput();
    }
}
