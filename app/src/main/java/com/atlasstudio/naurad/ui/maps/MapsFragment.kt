package com.atlasstudio.naurad.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.atlasstudio.naurad.R
import com.atlasstudio.naurad.databinding.FragmentMapsBinding
import com.atlasstudio.naurad.utils.showToast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

const val REQUEST_CODE_LOCATION = 42   // just a random unique number

@AndroidEntryPoint
class MapsFragment : Fragment(R.layout.fragment_maps),
                     OnMapReadyCallback,
                     OnMapClickListener,
                     OnMapLongClickListener,
                     OnCameraIdleListener
{
    private lateinit var mMap: GoogleMap
    private lateinit var mBinding: FragmentMapsBinding
    private lateinit var mTapTextView: TextView
    private lateinit var mCameraTextView: TextView

    private val viewModel: MapsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        observe()
        mBinding = FragmentMapsBinding.inflate(inflater, container, false)
        mTapTextView = mBinding.tapText
        mCameraTextView = mBinding.cameraText
        return mBinding.root
    }

    override fun onStart() {
        super.onStart()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapClickListener(this)
        mMap.setOnMapLongClickListener(this)
        mMap.setOnCameraIdleListener(this)

        mMap.setMapStyle(
            context?.let {
                MapStyleOptions.loadRawResourceStyle(
                    it, R.raw.mapstyle)
            })
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        enableCurrentLocation()

        // Add a marker in Zlin and move the camera
        val zlin = LatLng(49.230505, 17.657103)
        mMap.addMarker(MarkerOptions().position(viewModel.lastPosition() ?: zlin).title(getString(R.string.default_map_marker)))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(viewModel.lastPosition() ?: zlin, 14.5f))
    }

    override fun onMapClick(pos: LatLng) {
        mTapTextView.text = "tapped, point=$pos"

        mMap.clear()

        mMap.addMarker(MarkerOptions().position(pos))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 14.5f))

        viewModel.onPositionSelected(pos)
    }

    override fun onMapLongClick(point: LatLng) {
        mTapTextView.text = "long pressed, point=$point"
        mMap.clear()
        viewModel.onPositionSelected(null)
    }

    override fun onCameraIdle() {
        if(!::mMap.isInitialized) return
        mCameraTextView.text = mMap.cameraPosition.toString()
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

    private fun observeState(){
        viewModel.mState
            .flowWithLifecycle(lifecycle)
            .onEach { state ->
                handleState(state)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observe(){
        observeState()
        // and more...
    }

    private fun handleState(state: MapsFragmentState){
        when(state){
            is MapsFragmentState.IsLoading -> handleLoading(state.isLoading)
            is MapsFragmentState.ShowToast -> {
                mBinding.statusText.text = state.message
                requireActivity().showToast(state.message)
            }
            is MapsFragmentState.Init -> Unit
        }
    }

    private fun handleLoading(isLoading: Boolean) {
        if (isLoading) {
            mBinding.statusText.text = "loading..."
            mBinding.loadingProgressBar.visibility = View.VISIBLE
        } else {
            mBinding.statusText.text = "Loaded"
            mBinding.loadingProgressBar.visibility = View.GONE
        }
    }


    companion object {
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }
}