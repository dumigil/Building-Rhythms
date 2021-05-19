package com.example.buildingrhythms

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.view.Camera
import com.esri.arcgisruntime.mapping.view.SceneView
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

    private fun setupScene() {
        ArcGISRuntimeEnvironment.setApiKey("AAPKc0448a01c7d842c18fbdae5643d6ed65IgCmfZc9-fdYJPxtPca3UEp8Ft9V0zOulMBtumgxLfXxyG0wPadIluSBowA0JKCD")

        val scene = ArcGISScene(Basemap.createImagery())
        // set the scene on the scene view
        sceneView.scene = scene
        // add base surface for elevation data
        val elevationSource = ArcGISTiledElevationSource("https://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer")
        val surface = Surface(listOf(elevationSource))
        // add an exaggeration factor to increase the 3D effect of the elevation.
        surface.elevationExaggeration = 2.5f

        scene.baseSurface = surface
        // Point(x, y, z, spatialReference)
        val cameraLocation = Point(-118.794, 33.909, 5330.0, SpatialReferences.getWgs84())
        // Camera(location, heading, pitch, roll)
        val camera = Camera(cameraLocation, 355.0, 72.0, 0.0)

        sceneView.setViewpointCamera(camera)

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
}