package com.atlasstudio.naurad.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.atlasstudio.naurad.R
import com.atlasstudio.naurad.data.LocationWithOffices
import com.atlasstudio.naurad.data.OfficeType
import com.atlasstudio.naurad.databinding.FragmentMapsBinding
import com.atlasstudio.naurad.net.utils.ErrorResponseType
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
    private lateinit var mMenu: Menu
    private var mCurrentMarkers: MutableList<Marker?> = mutableListOf()

    private val viewModel: MapsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        mBinding = FragmentMapsBinding.inflate(inflater, container, false)
        mProgress = mBinding.progress
        return mBinding.root
    }

    override fun onStart() {
        super.onStart()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        viewModel.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        setFragmentResultListener("favourites_request") { _, bundle ->
            val result = bundle.getParcelable<LocationWithOffices>("favourites_result")
            viewModel.onFavouritesResult(result)
        }
    }

    @SuppressLint("MissingPermission")
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

        val zlin = LatLng(49.230505, 17.657103)
        /*var currentLocation: LatLng? = null
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (mMap.isMyLocationEnabled) {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    currentLocation = LatLng(task.result.latitude, task.result.longitude)
                }*/
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(viewModel.lastPosition /*?: (currentLocation*/ ?: zlin/*)*/, /*viewModel.lastZoom*/14.5f))
            /*}
        }*/

        observe()
        // re-place markers
        viewModel.onReady()

        mProgress.hide()
    }

    override fun onResume() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.onResume()
        super.onResume()
    }

    override fun onPause() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.onLowMemory()
        super.onLowMemory()
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

        handleFavourite(false)
        viewModel.onPositionSelected(pos)
    }

    override fun onCameraIdle() {
        if(!::mMap.isInitialized) return
        //mCameraTextView.text = mMap.cameraPosition.toString()
        /*if(viewModel.lastZoom != mMap.cameraPosition.zoom) {
            viewModel.onCameraZoomChanged(mMap.cameraPosition.zoom)
        }*/
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
            is MapsFragmentState.ShowTypeToast -> {
                //mBinding.statusText.text = state.message
                requireActivity().showToast(when(state.error) {
                    ErrorResponseType.LocationForOfficeError ->
                        getString(R.string.location_for_office_error)
                    ErrorResponseType.AddressForOfficeError ->
                        getString(R.string.address_for_office_error)
                    ErrorResponseType.OfficesNotFoundError ->
                        getString(R.string.office_not_found_error)
                    ErrorResponseType.AddressForLocationError ->
                        getString(R.string.address_for_location_error)
                    ErrorResponseType.LocationTranslationError ->
                        getString(R.string.location_translation_error)
                })
            }
            is MapsFragmentState.SetMarkers -> handleMarkers(state.location)
            is MapsFragmentState.IsFavourite -> handleFavourite(state.isFavourite)
            is MapsFragmentState.NavigateToFavourites -> handleShowFavourites()
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

    private fun handleMarkers(lwo: LocationWithOffices) {
        /*for(marker in mCurrentMarkers) {
            marker?.remove()
        }*/
        mCurrentMarkers.clear()
        mMap.clear()

        val cameraBounds: LatLngBounds.Builder = LatLngBounds.builder()
        // add office markers
        for( marker in lwo.offices) {
                val mark = mMap.addMarker(
                    MarkerOptions()
                        .position(marker.location)
                        .title(marker.name)
                        .icon(when(marker.type) { // this should not be here!!!
                            OfficeType.CityGovernmentOffice ->
                                    generateSmallIcon(context!!, R.drawable.ic_city_office)
                                OfficeType.LabourOffice ->
                                    generateSmallIcon(context!!, R.drawable.ic_labour_office)
                                OfficeType.TaxOffice ->
                                    generateSmallIcon(context!!, R.drawable.ic_tax_office)
                                OfficeType.CustomsOffice ->
                                    generateSmallIcon(context!!, R.drawable.ic_customs_office)
                                OfficeType.HighCourt ->
                                    generateSmallIcon(context!!, R.drawable.ic_high_court)
                                OfficeType.RegionalCourt ->
                                    generateSmallIcon(context!!, R.drawable.ic_regional_court)
                                OfficeType.DistrictCourt ->
                                    generateSmallIcon(context!!, R.drawable.ic_district_court)
                        }))
                mCurrentMarkers.add(mark)
                cameraBounds.include(marker.location)
        }

        // add selected location marker
            val mark = mMap.addMarker(MarkerOptions()
                .position(lwo.location.location)
                .title(lwo.location.address))
            mCurrentMarkers.add(mark)
            cameraBounds.include(lwo.location.location)

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(cameraBounds.build(), 50))
    }

    private fun handleFavourite(favourite: Boolean) {
        val favouriteMark = mMenu.findItem(R.id.action_mark_favourite)
        favouriteMark.isChecked = favourite
        favouriteMark.icon = AppCompatResources.getDrawable(context!!, if (favouriteMark.isChecked) R.drawable.ic_star else R.drawable.ic_star_border)
    }

    private fun handleShowFavourites() {
        val action = MapsFragmentDirections.actionMapsFragmentToFavouritesFragment()
        findNavController().navigate(action)
    }

    private fun generateSmallIcon(context: Context, resource: Int): BitmapDescriptor {
        val drawable = AppCompatResources.getDrawable(context, resource)

        if (drawable is BitmapDrawable) {
            return BitmapDescriptorFactory.fromBitmap(drawable.bitmap)
        }

        val bitmap = Bitmap.createBitmap(
            drawable?.intrinsicWidth ?: 50,
            drawable?.intrinsicHeight ?: 50,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_maps, menu)
        mMenu = menu

        /*val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.onQueryTextChanged {

        }*/
        viewModel.checkCurrentLocationFavourite()

        //val autoComplete = searchItem.actionView as SearchView.SearchAutoComplete

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            /*R.id.action_search -> {
                true
            }*/
            R.id.action_mark_favourite -> {
                viewModel.lastPosition?.let {
                    if (!mBinding.progress.isShown) {
                        item.isChecked = !item.isChecked
                        if (item.isChecked) {
                            item.icon = AppCompatResources.getDrawable(context!!, R.drawable.ic_star)
                            viewModel.storeCurrentLocation()
                        } else {
                            item.icon = AppCompatResources.getDrawable(context!!, R.drawable.ic_star_border)
                            viewModel.deleteCurrentLocation()
                        }
                    }
                }
                true
            }
            R.id.action_show_favourites -> {
                viewModel.onShowFavourites()
                true
            }
            else -> onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
    }
}