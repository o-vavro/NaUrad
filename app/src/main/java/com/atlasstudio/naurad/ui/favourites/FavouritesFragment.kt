package com.atlasstudio.naurad.ui.favourites

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.atlasstudio.naurad.R
import com.atlasstudio.naurad.data.AddressedLocationWithOffices
import com.atlasstudio.naurad.databinding.FragmentFavouritesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class FavouritesFragment : Fragment(R.layout.fragment_favourites) {
    private val viewModel: FavouritesViewModel by viewModels()
    private lateinit var mBinding: FragmentFavouritesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onStart()

        mBinding = FragmentFavouritesBinding.bind(view)
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

    private fun handleState(state: FavouritesFragmentState){
        when(state){
            is FavouritesFragmentState.NavigateBackWithResult -> handleNavigateBack(state.location)
            is FavouritesFragmentState.Init -> Unit
        }
    }

    private fun handleNavigateBack(location: AddressedLocationWithOffices) {
        mBinding.recyclerViewFavourites.clearFocus()
        setFragmentResult(
            "favourites_request",
            bundleOf("favourites_result" to location)
        )
        findNavController().popBackStack()
    }

}