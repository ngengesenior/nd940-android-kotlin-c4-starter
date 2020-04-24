package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.findFragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.text.DecimalFormat

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private val LOCATION_PERMISSION_REQUEST = 100
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val DEFAULT_ZOOM = 18.0f
    private lateinit var locationRequest: LocationRequest

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map

//        TODO: put a marker to location that the user selected


        return binding.root
    }


    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap
        enableMyLocation()
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            if (it.isSuccessful) {
                val location = it.result
                if (location != null) {
                    val latLong = LatLng(location.latitude, location.longitude)
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLong, DEFAULT_ZOOM)
                    map.moveCamera(cameraUpdate)
                    map.addMarker(
                        MarkerOptions().position(latLong)
                            .title("Your location")
                    )


                } else {
                    _viewModel.showErrorMessage.postValue("Location is null")
                }


            } else {

                _viewModel.showErrorMessage.postValue("Error getting last known location ${it.exception?.message}")
            }
        }

        //        TODO: call this function after the user confirms on the selected location
        onLocationSelected()
        selectArbitraryLocation()
        setMapStyle(map)


    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        map.setOnPoiClickListener { poi ->
            _viewModel.showToast.postValue("Point of interest selected")
            _viewModel.selectedPOI.postValue(poi)
            _viewModel.latitude.postValue(poi.latLng.latitude)
            _viewModel.longitude.postValue(poi.latLng.longitude)
            _viewModel.reminderSelectedLocationStr.postValue(poi.name)
            _viewModel.navigationCommand.postValue(
                NavigationCommand.Back
            )

        }
    }

    /**
     * Selects any point on map
     */
    private fun selectArbitraryLocation() {
        map.setOnMapClickListener {

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(it, DEFAULT_ZOOM)
            map.moveCamera(cameraUpdate)
            map.addMarker(MarkerOptions().position(it))
            _viewModel.latitude.postValue(it.latitude)
            _viewModel.longitude.postValue(it.longitude)

            AlertDialog.Builder(requireContext())
                .setTitle("Selected location")
                .setMessage("Save location with latitude and longitude ${it.latitude} and ${it.longitude} respectively?")
                .setPositiveButton(
                    "Yes"
                ) { _, _ ->
                    _viewModel.reminderSelectedLocationStr.postValue(
                        setLocationNameWithLatitudeAndLongitude(
                            it
                        )
                    )
                    _viewModel.navigationCommand.postValue(
                        NavigationCommand.Back
                    )
                }
                .setNegativeButton("No"
                ) { dialog, _ ->
                    dialog.dismiss()
                }.show()


        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.Done
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }


    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style

                )
            )

            if (!success) {
                //Log.d(TAG,"Style parsing failed")
                _viewModel.showErrorMessage.postValue("Map style parsing failed")
            }
        } catch (e: Resources.NotFoundException) {
            _viewModel.showErrorMessage.postValue("Can't find style ")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
        }

    }


    private fun setLocationNameWithLatitudeAndLongitude(latLng: LatLng):String{
        val df = DecimalFormat("#.###")
        return "LatLng(${df.format(latLng.latitude)},${df.format(latLng.longitude)})"
    }




}
