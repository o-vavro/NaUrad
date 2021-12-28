package com.atlasstudio.naurad.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.atlasstudio.naurad.R
import com.atlasstudio.naurad.databinding.FragmentMapsBinding
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
class MapsFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var mBinding: FragmentMapsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        mBinding = FragmentMapsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onStart() {
        super.onStart()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapStyle(
            context?.let {
                MapStyleOptions.loadRawResourceStyle(
                    it, R.raw.mapstyle)
            })
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
        if (hasLocationPermission() == true) {
            mMap.isMyLocationEnabled = true
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.location_permissions),
                REQUEST_CODE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun hasLocationPermission(): Boolean? {
        return context?.let { EasyPermissions.hasPermissions(it, Manifest.permission.ACCESS_FINE_LOCATION) }
    }

    companion object {
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }
}