import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;

import java.io.File;
import java.util.Collections;

public class SetupSoot{

	public static final String TAG = "<SetupSoot_TAG>";

    public static void setupSoot(String androidJar, String apkPath, String outputPath){
        G.reset();
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_whole_program(true);
        Options.v().set_prepend_classpath(true);
        Options.v().validate();
        Options.v().src_prec(Options.src_prec_apk);
        Options.v().output_format(Options.output_format_dex);
        Options.v().android_jars(androidJar);
        Options.v().process_dir(Collections.singletonList(apkPath));
        Options.v().include_all(true);
        Options.v().process_multiple_dex(true);
        Options.v().output_dir(outputPath);
        Scene.v().addBasicClass("java.io.PrintSteam", SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
        Scene.v().loadNecessaryClasses();
    }
}
