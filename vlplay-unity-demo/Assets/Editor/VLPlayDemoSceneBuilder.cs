using UnityEditor;
using UnityEditor.SceneManagement;
using UnityEngine;

/// <summary>
/// Headless scaffolding for the demo project: builds the Main scene wired to
/// <see cref="VLPlayDemoConsole"/> and runs a synchronous facade smoke check.
/// Invoked via -executeMethod so the T0 skeleton is reproducible without the GUI.
/// </summary>
public static class VLPlayDemoSceneBuilder
{
    private const string SceneDir = "Assets/Scenes";
    private const string ScenePath = SceneDir + "/Main.unity";

    [MenuItem("VLPlay/Build Demo Scene")]
    public static void Build()
    {
        if (!AssetDatabase.IsValidFolder(SceneDir))
            AssetDatabase.CreateFolder("Assets", "Scenes");

        var scene = EditorSceneManager.NewScene(NewSceneSetup.DefaultGameObjects, NewSceneMode.Single);
        new GameObject("VLPlayDemo").AddComponent<VLPlayDemoConsole>();

        EditorSceneManager.SaveScene(scene, ScenePath);
        EditorBuildSettings.scenes = new[] { new EditorBuildSettingsScene(ScenePath, true) };
        Debug.Log("[VLPlayDemo] Built scene at " + ScenePath);
    }

    // Note: no headless facade smoke here — VLPlaySDK.Init() spins up the
    // MainThreadDispatcher MonoBehaviour (DontDestroyOnLoad), which is play-mode
    // only. The Play-Mode mock is verified in the Editor GUI (press Play → Login)
    // or on device (U1); an edit-mode -executeMethod can't exercise it.
}
