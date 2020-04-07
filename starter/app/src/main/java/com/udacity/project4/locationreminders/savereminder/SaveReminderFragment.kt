package com.udacity.project4.locationreminders.savereminder

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.GEOFENCE_EXPIRATION_IN_MILLISECONDS
import com.udacity.project4.utils.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.utils.GEO_FENCE_EVENT
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient

    private val geoFencePendingIntent:PendingIntent by lazy {
        val intent = Intent(requireActivity(),GeofenceBroadcastReceiver::class.java)
        intent.action = GEO_FENCE_EVENT
        PendingIntent.getBroadcast(requireContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)
        geofencingClient = LocationServices.getGeofencingClient(requireContext())

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {

            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value



            latitude?.let {
                addGeoFence(it,longitude!!)
            }

            _viewModel.validateAndSaveReminder(
                ReminderDataItem(
                    title,description,location,latitude,longitude
                )
            )
            //_viewModel.navigationCommand.postValue(NavigationCommand.Back)


//            TODO: use the user entered reminder details to:
//             1) add a geofencing request

//             2) save the reminder to the local db
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private fun addGeoFence(latitude:Double,longitude:Double) {
        val geoFenceAddedMessage = getString(R.string.geofence_added)
        val geofenceAddedFailed = getString(R.string.error_adding_geofence)
        val geofence = Geofence.Builder()
            .setRequestId(UUID.randomUUID().toString())
            .setCircularRegion(latitude,
                longitude,
                GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.removeGeofences(geoFencePendingIntent)?.run {
            addOnCompleteListener{
                geofencingClient.addGeofences(geofenceRequest,geoFencePendingIntent)?.run {

                    addOnSuccessListener {

                        _viewModel.showToast.postValue(geoFenceAddedMessage)
                    }

                    addOnFailureListener {

                        _viewModel.showErrorMessage.postValue(geofenceAddedFailed)
                    }
                }

            }
        }
    }
}