package com.atlasstudio.naurad.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.atlasstudio.naurad.R
import com.atlasstudio.naurad.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

const val REQUEST_CODE_LOCATION = 42   // just a random unique number

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    //private lateinit var navController: NavController
    private lateinit var mMap: GoogleMap
    private lateinit var mBinding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        mBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        //val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        //navController = navHostFragment.findNavController()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
            this, R.raw.mapstyle))
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true

        enableCurrentLocation()

        // Add a marker in Zlin and move the camera
        val zlin = LatLng(49.230505, 17.657103)
        mMap.addMarker(MarkerOptions().position(zlin).title(getString(R.string.default_map_marker)))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(zlin, 14.5f))

        // call a method that will handle location change
        // data processing class - View + ViewModel
        // - GPS to JTSK
        // - ...
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(REQUEST_CODE_LOCATION)
    private fun enableCurrentLocation() {
        if (hasLocationPermission()) {
            mMap.isMyLocationEnabled = true
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.location_permissions),
                REQUEST_CODE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)
    }
}