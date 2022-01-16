package com.atlasstudio.naurad.ui.favourites

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atlasstudio.naurad.R
import com.atlasstudio.naurad.data.AddressedLocationWithOffices
import com.atlasstudio.naurad.data.TouchedLocation
import com.atlasstudio.naurad.databinding.FragmentFavouritesBinding
import com.atlasstudio.naurad.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavouritesFragment : Fragment(R.layout.fragment_favourites), FavouritesAdapter.OnItemClickListener {
    private val viewModel: FavouritesViewModel by viewModels()
    private lateinit var mBinding: FragmentFavouritesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onStart()

        mBinding = FragmentFavouritesBinding.bind(view)

        val favouritesAdapter = FavouritesAdapter(this)

        mBinding.apply {
            recyclerViewFavourites.apply {
                adapter = favouritesAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val favourite = favouritesAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onFavouriteDelete(favourite)
                }
            }).attachToRecyclerView(recyclerViewFavourites)
        }

        lifecycle.coroutineScope.launch {
            viewModel.createFavouritesFlow().collect() {
                favouritesAdapter.submitList(it)
            }
        }

        observe()
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
            is FavouritesFragmentState.SnackBarFavouriteDeleted -> handleDeletedSnackBar()
        }
    }

    override fun onItemClick(favourite: TouchedLocation) {
        viewModel.onFavouriteSelected(favourite)
    }

    override fun onButtonDeleteClick(favourite: TouchedLocation) {
        viewModel.onFavouriteDelete(favourite)
    }

    private fun handleNavigateBack(location: AddressedLocationWithOffices) {
        mBinding.recyclerViewFavourites.clearFocus()
        setFragmentResult(
            "favourites_request",
            bundleOf("favourites_result" to location)
        )
        findNavController().popBackStack()
    }

    private fun handleDeletedSnackBar() {
        requireActivity().showToast(getString(R.string.location_deleted_confirmation))
    }
}