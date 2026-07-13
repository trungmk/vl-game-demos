using System;
using System.IO;
using System.Linq;
using System.Reflection;
using UnityEditor;
using UnityEditor.Build;
using UnityEditor.Build.Reporting;
using UnityEngine;

/// <summary>
/// Batchmode entry points for the U1-A-8 device build.
///   Unity -batchmode -quit -projectPath . -buildTarget Android \
///         -executeMethod VLPlayDemoBuild.BuildAndroid
/// </summary>
public static class VLPlayDemoBuild
{
    private const string OutputDir = "build";
    private const string ApkName = "vlplay-demo.apk";

    public static void BuildAndroid()
    {
        ResolveAndroidDependencies();

        string[] scenes = EditorBuildSettings.scenes
            .Where(s => s.enabled)
            .Select(s => s.path)
            .ToArray();
        if (scenes.Length == 0)
        {
            Fail("no enabled scenes in EditorBuildSettings");
            return;
        }

        Directory.CreateDirectory(OutputDir);
        string apk = Path.Combine(OutputDir, ApkName);

        EditorUserBuildSettings.buildAppBundle = false; // APK, not AAB

        Debug.Log("[VLPlayBuild] appId=" + PlayerSettings.GetApplicationIdentifier(NamedBuildTarget.Android)
                  + " backend=" + PlayerSettings.GetScriptingBackend(NamedBuildTarget.Android)
                  + " arch=" + PlayerSettings.Android.targetArchitectures
                  + " entry=" + PlayerSettings.Android.applicationEntry
                  + " minSdk=" + PlayerSettings.Android.minSdkVersion
                  + " scenes=" + string.Join(",", scenes));

        var opts = new BuildPlayerOptions
        {
            scenes = scenes,
            locationPathName = apk,
            target = BuildTarget.Android,
            targetGroup = BuildTargetGroup.Android,
            options = BuildOptions.Development | BuildOptions.AllowDebugging,
        };

        BuildSummary s = BuildPipeline.BuildPlayer(opts).summary;
        Debug.Log("[VLPlayBuild] result=" + s.result + " errors=" + s.totalErrors
                  + " size=" + s.totalSize + " time=" + s.totalTime);

        if (s.result != BuildResult.Succeeded)
        {
            Fail("build " + s.result + " (" + s.totalErrors + " error(s))");
            return;
        }

        Debug.Log("[VLPlayBuild] APK OK: " + Path.GetFullPath(apk));
        EditorApplication.Exit(0);
    }

    /// <summary>
    /// U1-A-9: IL2CPP RELEASE smoke. Non-development build with managed stripping
    /// forced to High — the worst case a shipping game can configure. If link.xml
    /// under-preserves, the AndroidJavaProxy listener subclasses and the
    /// UnitySendMessage callback target get stripped and the SDK's callbacks go
    /// silent at runtime: no crash, no log, just nothing firing.
    /// </summary>
    public static void BuildAndroidRelease()
    {
        ResolveAndroidDependencies();

        PlayerSettings.SetManagedStrippingLevel(NamedBuildTarget.Android, ManagedStrippingLevel.High);

        string[] scenes = EditorBuildSettings.scenes
            .Where(s => s.enabled).Select(s => s.path).ToArray();

        Directory.CreateDirectory(OutputDir);
        string apk = Path.Combine(OutputDir, "vlplay-demo-release.apk");

        EditorUserBuildSettings.buildAppBundle = false;

        Debug.Log("[VLPlayBuild] RELEASE appId=" + PlayerSettings.GetApplicationIdentifier(NamedBuildTarget.Android)
                  + " backend=" + PlayerSettings.GetScriptingBackend(NamedBuildTarget.Android)
                  + " stripping=" + PlayerSettings.GetManagedStrippingLevel(NamedBuildTarget.Android));

        var opts = new BuildPlayerOptions
        {
            scenes = scenes,
            locationPathName = apk,
            target = BuildTarget.Android,
            targetGroup = BuildTargetGroup.Android,
            options = BuildOptions.None, // release: no Development, stripping active
        };

        BuildSummary s = BuildPipeline.BuildPlayer(opts).summary;
        Debug.Log("[VLPlayBuild] result=" + s.result + " errors=" + s.totalErrors + " time=" + s.totalTime);

        if (s.result != BuildResult.Succeeded)
        {
            Fail("release build " + s.result + " (" + s.totalErrors + " error(s))");
            return;
        }

        Debug.Log("[VLPlayBuild] RELEASE APK OK: " + Path.GetFullPath(apk));
        EditorApplication.Exit(0);
    }

    /// <summary>
    /// Exports the Xcode project (U1-B-6). EDM4U generates the Podfile and runs
    /// pod install; the VLPlay post-processor injects the local VLPlaySDK pod
    /// (order 41) and the plists (order 61).
    /// </summary>
    public static void BuildIOS()
    {
        // (intentionally NOT forcing linkage here — the SDK post-processor must handle it)

        string[] scenes = EditorBuildSettings.scenes
            .Where(s => s.enabled)
            .Select(s => s.path)
            .ToArray();
        if (scenes.Length == 0)
        {
            Fail("no enabled scenes in EditorBuildSettings");
            return;
        }

        string outDir = Path.Combine(OutputDir, "ios");
        Directory.CreateDirectory(outDir);

        Debug.Log("[VLPlayBuild] iOS appId=" + PlayerSettings.GetApplicationIdentifier(NamedBuildTarget.iOS)
                  + " backend=" + PlayerSettings.GetScriptingBackend(NamedBuildTarget.iOS)
                  + " target=" + PlayerSettings.iOS.targetOSVersionString
                  + " out=" + outDir);

        var opts = new BuildPlayerOptions
        {
            scenes = scenes,
            locationPathName = outDir,
            target = BuildTarget.iOS,
            targetGroup = BuildTargetGroup.iOS,
            options = BuildOptions.Development | BuildOptions.AllowDebugging,
        };

        BuildSummary s = BuildPipeline.BuildPlayer(opts).summary;
        Debug.Log("[VLPlayBuild] result=" + s.result + " errors=" + s.totalErrors + " time=" + s.totalTime);

        if (s.result != BuildResult.Succeeded)
        {
            Fail("iOS export " + s.result + " (" + s.totalErrors + " error(s))");
            return;
        }

        Debug.Log("[VLPlayBuild] Xcode project OK: " + Path.GetFullPath(outDir));
        EditorApplication.Exit(0);
    }

    /// <summary>
    /// EDM4U defaults to `use_frameworks! :linkage => :static`. FBSDK 18 and
    /// OneSignal 5.x ship as prebuilt DYNAMIC xcframeworks, which CocoaPods
    /// cannot re-link statically — so under static linkage they stay dynamic
    /// but no `[CP] Embed Pods Frameworks` phase is generated, and the app
    /// dies at launch with `dyld: Library not loaded: @rpath/FBSDKCoreKit…`.
    /// Dynamic linkage restores the embed phase.
    /// </summary>
    private static void UseDynamicPodLinkage()
    {
        Type r = AppDomain.CurrentDomain.GetAssemblies()
            .SelectMany(SafeTypes)
            .FirstOrDefault(t => t.FullName == "Google.IOSResolver");
        if (r == null)
        {
            Debug.LogWarning("[VLPlayBuild] Google.IOSResolver not found — pod linkage unchanged.");
            return;
        }

        PropertyInfo p = r.GetProperty("PodfileStaticLinkFrameworks",
                                       BindingFlags.Public | BindingFlags.Static);
        if (p == null || !p.CanWrite)
        {
            Debug.LogWarning("[VLPlayBuild] PodfileStaticLinkFrameworks not settable — pod linkage unchanged.");
            return;
        }

        p.SetValue(null, false);
        Debug.Log("[VLPlayBuild] EDM4U PodfileStaticLinkFrameworks=false (dynamic — embeds FBSDK/OneSignal).");
    }

    /// <summary>
    /// EDM4U force-resolve, invoked by reflection so this script carries no
    /// compile-time dependency on the external-dependency-manager package.
    /// </summary>
    private static void ResolveAndroidDependencies()
    {
        Type resolver = AppDomain.CurrentDomain.GetAssemblies()
            .SelectMany(SafeTypes)
            .FirstOrDefault(t => t.FullName == "GooglePlayServices.PlayServicesResolver");
        if (resolver == null)
        {
            Fail("EDM4U PlayServicesResolver not found — is com.google.external-dependency-manager resolved?");
            return;
        }

        MethodInfo resolve = resolver.GetMethod(
            "ResolveSync",
            BindingFlags.Public | BindingFlags.Static,
            null,
            new[] { typeof(bool) },
            null);
        if (resolve == null)
        {
            Fail("EDM4U ResolveSync(bool) not found");
            return;
        }

        Debug.Log("[VLPlayBuild] EDM4U force-resolve ...");
        resolve.Invoke(null, new object[] { true });
        AssetDatabase.Refresh();
        Debug.Log("[VLPlayBuild] EDM4U resolve done.");
    }

    private static Type[] SafeTypes(Assembly a)
    {
        try { return a.GetTypes(); }
        catch { return Type.EmptyTypes; }
    }

    private static void Fail(string msg)
    {
        Debug.LogError("[VLPlayBuild] FAILED: " + msg);
        EditorApplication.Exit(1);
    }
}
