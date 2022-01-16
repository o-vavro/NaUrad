package com.atlasstudio.naurad.ui.favourites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.atlasstudio.naurad.data.TouchedLocation
import com.atlasstudio.naurad.databinding.ItemFavouritesBinding

class FavouritesAdapter(private val listener: OnItemClickListener) :
    ListAdapter<TouchedLocation, FavouritesAdapter.FavouritesViewHolder>(DiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritesViewHolder {
            val binding = ItemFavouritesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return FavouritesViewHolder(binding)
        }

        override fun onBindViewHolder(holder: FavouritesViewHolder, position: Int) {
            val currentItem = getItem(position)
            holder.bind(currentItem)
        }

        inner class FavouritesViewHolder(private val binding: ItemFavouritesBinding) :
            RecyclerView.ViewHolder(binding.root) {

            init {
                binding.apply {
                    labelLocation.setOnClickListener {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val favourite = getItem(position)
                            listener.onItemClick(favourite)
                        }
                    }

                    textViewName.setOnClickListener {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val favourite = getItem(position)
                            listener.onItemClick(favourite)
                        }
                    }

                    buttonDelete.setOnClickListener {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val favourite = getItem(position)
                            listener.onButtonDeleteClick(favourite)
                        }
                    }
                }
            }

            fun bind(location: TouchedLocation) {
                binding.apply {
                    textViewName.text = location.address
                }
            }
        }

        interface OnItemClickListener {
            fun onItemClick(favourite: TouchedLocation)
            fun onButtonDeleteClick(favourite: TouchedLocation)
        }

        class DiffCallback : DiffUtil.ItemCallback<TouchedLocation>() {
            override fun areItemsTheSame(oldItem: TouchedLocation, newItem: TouchedLocation) =
                oldItem.location == newItem.location

            override fun areContentsTheSame(oldItem: TouchedLocation, newItem: TouchedLocation) =
                oldItem == newItem
        }
}