package com.atlasstudio.naurad.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.atlasstudio.naurad.R
import com.atlasstudio.naurad.data.Office
import com.atlasstudio.naurad.data.OfficeType
import com.atlasstudio.naurad.databinding.FragmentMapsBinding
import com.atlasstudio.naurad.utils.showToast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.progressindicator.LinearProgressIndicator
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
    private lateinit var mProgress: LinearProgressIndicator
    //private lateinit var mTapTextView: TextView
    //private lateinit var mCameraTextView: TextView
    private var mCurrentMarkers: MutableList<Marker?> = mutableListOf()

    private val viewModel: MapsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        observe()
        mBinding = FragmentMapsBinding.inflate(inflater, container, false)
        mProgress = mBinding.progress
        //mTapTextView = mBinding.tapText
        //mCameraTextView = mBinding.cameraText
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

        mProgress.hide()
    }

    override fun onMapClick(pos: LatLng) {
        //mTapTextView.text = "tapped, point=$pos"

        //mMap.clear()

        //mMap.addMarker(MarkerOptions().position(pos))
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 14.5f))

        //viewModel.onPositionSelected(pos)
    }

    override fun onMapLongClick(pos: LatLng) {
        //mTapTextView.text = "long pressed, point=$point"
        mMap.clear()

        mMap.addMarker(MarkerOptions().position(pos))
        mMap.animateCamera(CameraUpdateFactory.newLatLng(pos))

        viewModel.onPositionSelected(pos)
    }

    override fun onCameraIdle() {
        if(!::mMap.isInitialized) return
        //mCameraTextView.text = mMap.cameraPosition.toString()
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
                //mBinding.statusText.text = state.message
                requireActivity().showToast(state.message)
            }
            is MapsFragmentState.SetMarkers -> handleMarkers(state.markers)
            is MapsFragmentState.Init -> Unit
        }
    }

    private fun handleLoading(isLoading: Boolean) {
        if (isLoading) {
            //mBinding.statusText.text = "loading..."
            mBinding.progress.show()
        } else {
            //mBinding.statusText.text = "Loaded"
            mBinding.progress.hide()
        }
    }

    private fun handleMarkers(markers: List<Office?>) {
        for(marker in mCurrentMarkers) {
            marker?.remove()
        }
        mCurrentMarkers.clear()

        var cameraBounds: LatLngBounds.Builder = LatLngBounds.builder()
        for( marker in markers) {
            marker?.let { marker ->
                val mark = mMap.addMarker(
                    MarkerOptions()
                        .position(marker.location)
                        .title(marker.name)
                        .icon(when(marker.type) { // this should not be here!!!
                            OfficeType.CityGovernmentOffice ->
                                    generateSmallIcon(context!!, R.drawable.ic_city_office_marker)
                                OfficeType.LabourOffice ->
                                    generateSmallIcon(context!!, R.drawable.ic_labour_office_marker)
                                OfficeType.TaxOffice ->
                                    generateSmallIcon(context!!, R.drawable.ic_tax_office_marker)
                                OfficeType.CustomsOffice ->
                                    generateSmallIcon(context!!, R.drawable.ic_customs_office_marker)
                                OfficeType.HighCourt ->
                                    generateSmallIcon(context!!, R.drawable.ic_high_court_marker)
                                OfficeType.RegionalCourt ->
                                    generateSmallIcon(context!!, R.drawable.ic_regional_court_marker)
                                OfficeType.DistrictCourt ->
                                    generateSmallIcon(context!!, R.drawable.ic_district_court_marker)
                        }))
                mCurrentMarkers.add(mark)
            }
            marker?.location?.let {
                cameraBounds.include(it)
            }
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(cameraBounds.build(), 50))
    }

    private fun generateSmallIcon(context: Context, resource: Int): BitmapDescriptor {
        val drawable = context.getDrawable(resource)

        if (drawable is BitmapDrawable) {
            return BitmapDescriptorFactory.fromBitmap(drawable.bitmap)
        }

        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable!!.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable!!.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
        drawable!!.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    companion object {
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }
}