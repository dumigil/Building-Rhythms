package com.example.buildingrhythms

import android.content.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.net.wifi.WifiManager
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.SceneView

import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.layers.ArcGISSceneLayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.*
import com.esri.arcgisruntime.mapping.view.MapView

import com.example.buildingrhythms.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val activityMainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val sceneView: SceneView by lazy {
        activityMainBinding.sceneView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(activityMainBinding.root)

        setupScene()

    }

    override fun onPause() {
        sceneView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        sceneView.resume()
    }

    override fun onDestroy() {
        sceneView.dispose()
        super.onDestroy()
    }

    // set up your scene here. You will call this method from onCreate()
    private fun setupScene() {

        // Note: it is not best practice to store API keys in source code.
        // The API key is referenced here for the convenience of this tutorial.
        ArcGISRuntimeEnvironment.setApiKey("AAPKc0448a01c7d842c18fbdae5643d6ed65IgCmfZc9-fdYJPxtPca3UEp8Ft9V0zOulMBtumgxLfXxyG0wPadIluSBowA0JKCD")
        val serviceFeatureTable = ServiceFeatureTable("https://services3.arcgis.com/jR9a3QtlDyTstZiO/arcgis/rest/services/BK_MAP_WFL1/FeatureServer/4")
        val featureLayer = FeatureLayer(serviceFeatureTable)

        val scene = ArcGISScene(Basemap.createLightGrayCanvasVector())
        scene.operationalLayers.add(featureLayer)
        // set the scene on the scene view
        sceneView.scene = scene

        // add base surface for elevation data
        val elevationSource = ArcGISTiledElevationSource("https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer")
        val surface = Surface(listOf(elevationSource))
        // add an exaggeration factor to increase the 3D effect of the elevation.

        scene.baseSurface = surface

        // Point(x, y, z, spatialReference)
        val cameraLocation = Point(4.371480, 52.006719, 720.0, SpatialReferences.getWgs84())
        // Camera(location, heading, pitch, roll)
        val camera = Camera(cameraLocation, 355.0, 0.0, 0.0)

        sceneView.setViewpointCamera(camera)
        WifiScanner()


    }
    private fun scanNetworks() {

    }

}